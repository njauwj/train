package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.*;
import com.wj.train.business.enums.ConfirmOrderStatusEnum;
import com.wj.train.business.enums.SeatColEnum;
import com.wj.train.business.enums.SeatTypeEnum;
import com.wj.train.business.feign.MemberFeign;
import com.wj.train.business.mapper.ConfirmOrderMapper;
import com.wj.train.business.mapper.DailyTrainSeatMapper;
import com.wj.train.business.mapper.DailyTrainTicketMapper;
import com.wj.train.business.req.ConfirmOrderQueryReq;
import com.wj.train.business.req.ConfirmOrderSaveReq;
import com.wj.train.business.req.TicketSaveReq;
import com.wj.train.business.resp.ConfirmOrderQueryResp;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.wj.train.common.exception.BusinessExceptionEnum.*;

@Service
@Slf4j
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private ConfirmOrderService confirmOrderService;

    @Resource
    private RedissonClient redissonClient;


    public void save(ConfirmOrderSaveReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowFlowUtil.getSnowFlowId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * @param confirmOrderSaveReq
     */
    public void confirmOrder(ConfirmOrderSaveReq confirmOrderSaveReq) {
        String key = confirmOrderSaveReq.getDate() + confirmOrderSaveReq.getTrainCode();
        RLock lock = redissonClient.getLock(key);
        try {
            boolean result = lock.tryLock(10, TimeUnit.SECONDS);
            if (!result) {
                throw new BusinessException(BUSINESS_TOO_MANY_PEOPLE);
            }
            DateTime now = DateTime.now();
            String trainCode = confirmOrderSaveReq.getTrainCode();
            Date date = confirmOrderSaveReq.getDate();
            ConfirmOrder confirmOrder = BeanUtil.copyProperties(confirmOrderSaveReq, ConfirmOrder.class);
            List<Ticket> tickets = confirmOrderSaveReq.getTickets();
            String ticketsJson = JSONUtil.toJsonStr(tickets);
            confirmOrder.setTickets(ticketsJson);
            confirmOrder.setId(SnowFlowUtil.getSnowFlowId());
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            //保存初始化确认订单信息
            confirmOrderMapper.insert(confirmOrder);
            //查询余票
            DailyTrainTicket dailyTrainTicket = dailyTrainTicketMapper.selectByPrimaryKey(confirmOrderSaveReq.getDailyTrainTicketId());
            //预扣减库存
            reduceTickets(confirmOrder, tickets, dailyTrainTicket);
            //查看用户是否选座
            String seat = tickets.get(0).getSeat();
            //存储最终地选座结果
            ArrayList<DailyTrainSeat> finalChooseSeats = new ArrayList<>();
            if (CharSequenceUtil.isNotBlank(seat)) {
                //有选座，要求是前后连续两排，同车厢，同类型的座位
                log.info("本次购票有选座");
                String seatTypeCode = tickets.get(0).getSeatTypeCode();
                List<SeatColEnum> colsByType = SeatColEnum.getColsByType(seatTypeCode);
                //构造出前后连续的两排
                ArrayList<String> cowList = new ArrayList<>();
                for (int i = 1; i <= 2; i++) {
                    for (SeatColEnum seatColEnum : colsByType) {
                        cowList.add(seatColEnum.getCode() + i);
                    }
                }
                log.info("前后两排座位{}", cowList);
                //计算选中座位的绝对偏移值
                ArrayList<Integer> absoluteOffset = new ArrayList<>();
                for (Ticket ticket : tickets) {
                    int index = cowList.indexOf(ticket.getSeat());
                    absoluteOffset.add(index);
                }
                log.info("选中座位的绝对偏移值{}", absoluteOffset);
                //计算与第一个座位的相对偏移值
                ArrayList<Integer> relativeOffset = new ArrayList<>();
                for (Integer offset : absoluteOffset) {
                    relativeOffset.add(offset - absoluteOffset.get(0));
                }
                log.info("选中座位的相对偏移值{}", relativeOffset);
                // seat.substring(0, 1) A1 -> A
                chooseSeat(finalChooseSeats, trainCode, date, seatTypeCode, seat.substring(0, 1), relativeOffset, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());
            } else {
                //没有选座,直接遍历所有车厢座位进行选座
                log.info("本次购票没有选座");
                for (Ticket ticket : tickets) {
                    chooseSeat(finalChooseSeats, trainCode, date, ticket.getSeatTypeCode(), null, null, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());
                }
            }
            try {
                confirmOrderService.updateFinalDataToDB(confirmOrderSaveReq, trainCode, date, confirmOrder, tickets, dailyTrainTicket, finalChooseSeats);
            } catch (Exception e) {
                confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                confirmOrder.setUpdateTime(DateTime.now());
                confirmOrderMapper.updateByPrimaryKey(confirmOrder);
                throw new BusinessException(BUSINESS_CONFIRM_ORDER_BUSY);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                log.info("释放锁");
                lock.unlock();
            }
        }


    }

    /**
     * 保存座位的详情数据，车站车票的扣减，购票信息至数据库
     * 由于涉及不同数据库之间的调用，所以需要使用分布式事务
     *
     * @param confirmOrderSaveReq
     * @param trainCode
     * @param date
     * @param confirmOrder
     * @param tickets
     * @param dailyTrainTicket
     * @param finalChooseSeats
     */
//    @GlobalTransactional
    public void updateFinalDataToDB(ConfirmOrderSaveReq confirmOrderSaveReq, String trainCode, Date date, ConfirmOrder confirmOrder, List<Ticket> tickets, DailyTrainTicket dailyTrainTicket, ArrayList<DailyTrainSeat> finalChooseSeats) {

//        log.info("seata全局事务ID: {}", RootContext.getXID());
        log.info("保存最终的选座结果至数据库，并扣减车票数量");
        updateFinalChooseSeatsToDb(dailyTrainTicket, finalChooseSeats);
        //更待订单状态由初始->成功
        ConfirmOrder finalCOnfirmOrder = new ConfirmOrder();
        finalCOnfirmOrder.setId(confirmOrder.getId());
        finalCOnfirmOrder.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
        finalCOnfirmOrder.setUpdateTime(DateTime.now());
        confirmOrderMapper.updateByPrimaryKeySelective(finalCOnfirmOrder);
        //保存会员购票信息
        for (int i = 0; i < finalChooseSeats.size(); i++) {
            TicketSaveReq ticketSaveReq = new TicketSaveReq();
            ticketSaveReq.setMemberId(confirmOrderSaveReq.getMemberId());
            ticketSaveReq.setPassengerId(tickets.get(i).getPassengerId());
            ticketSaveReq.setPassengerName(tickets.get(i).getPassengerName());
            ticketSaveReq.setTrainDate(date);
            ticketSaveReq.setTrainCode(trainCode);
            ticketSaveReq.setCarriageIndex(finalChooseSeats.get(i).getCarriageIndex());
            ticketSaveReq.setSeatRow(finalChooseSeats.get(i).getRow());
            ticketSaveReq.setSeatCol(finalChooseSeats.get(i).getCol());
            ticketSaveReq.setStartStation(dailyTrainTicket.getStart());
            ticketSaveReq.setStartTime(dailyTrainTicket.getStartTime());
            ticketSaveReq.setEndStation(dailyTrainTicket.getEnd());
            ticketSaveReq.setEndTime(dailyTrainTicket.getEndTime());
            ticketSaveReq.setSeatType(finalChooseSeats.get(i).getSeatType());
            memberFeign.save(ticketSaveReq);
        }
    }


    /**
     * 保存最终地选座结果至数据库，并扣减余票的库存
     * 注意：事务方法必须是public不然会失效
     */
//    @Transactional
    public void updateFinalChooseSeatsToDb(DailyTrainTicket dailyTrainTicket, List<DailyTrainSeat> finalChooseSeats) {
        log.info("---------最终的选座情况为----------");
        for (DailyTrainSeat finalChooseSeat : finalChooseSeats) {
            log.info("座位{}被选中", finalChooseSeat.getCarriageSeatIndex());
            //sell 更新后的售卖信息
            String sell = finalChooseSeat.getSell();
            String seatType = finalChooseSeat.getSeatType();
            String trainCode = finalChooseSeat.getTrainCode();
            Date date = finalChooseSeat.getDate();
            DailyTrainSeat dailyTrainSeat = new DailyTrainSeat();
            dailyTrainSeat.setId(finalChooseSeat.getId());
            dailyTrainSeat.setSell(sell);
            dailyTrainSeat.setUpdateTime(DateTime.now());
            dailyTrainSeatMapper.updateByPrimaryKeySelective(dailyTrainSeat);
            //扣减余票
            /*
            假设10个站，本次买4~7站,站序从0开始
            原售：001000001
            购买：000011100
            新售：001011101
             */
            //起始站
            int startIndex = dailyTrainTicket.getStartIndex();
            //终点站
            int endIndex = dailyTrainTicket.getEndIndex();
            //新售：001011101
            char[] sellCharArray = sell.toCharArray();
            //minBegin 最大受影响区间的起始
            int minBegin = 0;
            //maxEnd 最大受影响区间的结束
            int maxEnd = sell.length() - 1;
            for (int i = startIndex - 1; i >= 0; i--) {
                if (sellCharArray[i] == '1') {
                    minBegin = i + 1;
                    break;
                }
            }
            for (int j = endIndex; j < sellCharArray.length; j++) {
                if (sellCharArray[j] == '1') {
                    maxEnd = j - 1;
                    break;
                }
            }
            for (int k = minBegin; k < endIndex; k++) {
                int temp = Math.max(k, startIndex);
                for (int m = temp; m <= maxEnd; m++) {
                    DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
                    dailyTrainTicketExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode)
                            .andStartIndexEqualTo(k).andEndIndexEqualTo(m + 1);
                    DailyTrainTicket dailyTrainTicketDB = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample).get(0);
                    SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatType);
                    //预扣减余票
                    switch (seatTypeEnum) {
                        case YDZ: {
                            DailyTrainTicket updateDailyTrainTicket = new DailyTrainTicket();
                            updateDailyTrainTicket.setId(dailyTrainTicketDB.getId());
                            updateDailyTrainTicket.setYdz(dailyTrainTicketDB.getYdz() - 1);
                            updateDailyTrainTicket.setUpdateTime(DateTime.now());
                            dailyTrainTicketMapper.updateByPrimaryKeySelective(updateDailyTrainTicket);
                            break;
                        }
                        case EDZ: {
                            DailyTrainTicket updateDailyTrainTicket = new DailyTrainTicket();
                            updateDailyTrainTicket.setId(dailyTrainTicketDB.getId());
                            updateDailyTrainTicket.setEdz(dailyTrainTicketDB.getEdz() - 1);
                            updateDailyTrainTicket.setUpdateTime(DateTime.now());
                            dailyTrainTicketMapper.updateByPrimaryKeySelective(updateDailyTrainTicket);
                            break;
                        }
                        case RW: {
                            DailyTrainTicket updateDailyTrainTicket = new DailyTrainTicket();
                            updateDailyTrainTicket.setId(dailyTrainTicketDB.getId());
                            updateDailyTrainTicket.setRw(dailyTrainTicketDB.getRw() - 1);
                            updateDailyTrainTicket.setUpdateTime(DateTime.now());
                            dailyTrainTicketMapper.updateByPrimaryKeySelective(updateDailyTrainTicket);
                            break;
                        }
                        case YW: {
                            DailyTrainTicket updateDailyTrainTicket = new DailyTrainTicket();
                            updateDailyTrainTicket.setId(dailyTrainTicketDB.getId());
                            updateDailyTrainTicket.setYw(dailyTrainTicketDB.getYw() - 1);
                            updateDailyTrainTicket.setUpdateTime(DateTime.now());
                            dailyTrainTicketMapper.updateByPrimaryKeySelective(updateDailyTrainTicket);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 进行座位的选择,seat进行选座的第一个座位类型，后面的座位类型可以由relativeOffset进行推算
     */
    private void chooseSeat(ArrayList<DailyTrainSeat> finalChooseSeats, String trainCode, Date date, String seatType, String seat, ArrayList<Integer> relativeOffset, Integer start, Integer end) {
        //根据座位的类型筛选出车厢
        List<DailyTrainCarriage> carriagesBySeatType = dailyTrainCarriageService.getCarriagesBySeatType(trainCode, date, seatType);
        log.info("符合的车厢数量为{}", carriagesBySeatType.size());
        //遍历每一个车厢找出所有座位
        for (DailyTrainCarriage dailyTrainCarriage : carriagesBySeatType) {
            Integer carriageIndex = dailyTrainCarriage.getIndex();
            List<DailyTrainSeat> trainSeats = dailyTrainSeatService.getSeatsByCarriageIndex(trainCode, date, carriageIndex);
            log.info("车厢{}座位数量{}", carriageIndex, trainSeats.size());
            //遍历该车厢的所有座位
            for (DailyTrainSeat trainSeat : trainSeats) {
                if (CharSequenceUtil.isNotBlank(seat)) {
                    //有选座的情况下
                    String col = trainSeat.getCol();
                    Integer seatIndex = trainSeat.getCarriageSeatIndex();
                    if (!col.equals(seat)) {
                        log.info("该座位{}对应的列值{}不对", seatIndex, col);
                        continue;
                    }
                    boolean result = canSell(trainSeat, start, end);
                    if (!result) {
                        continue;
                    }
                    finalChooseSeats.add(trainSeat);
                    boolean flag = false;
                    for (int i = 1; i < relativeOffset.size(); i++) {
                        //对剩下的座位进行选座
                        int position = seatIndex + relativeOffset.get(i);
                        if (position > trainSeats.get(trainSeats.size() - 1).getCarriageSeatIndex()) {
                            log.info("所选座位{}超过了当前车厢", position);
                            flag = true;
                            break;
                        }
                        DailyTrainSeat nextTrainSeat = trainSeats.get(position - 1);
                        boolean res = canSell(nextTrainSeat, start, end);
                        if (!res) {
                            flag = true;
                            break;
                        }
                        finalChooseSeats.add(nextTrainSeat);
                    }
                    if (flag) {
                        //清除所有已选择的座位进行下一轮选择
                        finalChooseSeats.clear();
                        continue;
                    }
                    return;
                } else {
                    boolean isChoosed = false;
                    for (DailyTrainSeat finalChooseSeat : finalChooseSeats) {
                        if (finalChooseSeat.getId().equals(trainSeat.getId())) {
                            log.info("该座位{}已经被选过不能重复选择", trainSeat.getCarriageSeatIndex());
                            isChoosed = true;
                        }
                    }
                    if (isChoosed) {
                        continue;
                    }
                    //无选座的情况下遍历每个座位看对应车站区间是否有座
                    boolean result = canSell(trainSeat, start, end);
                    if (result) {
                        finalChooseSeats.add(trainSeat);
                        return;
                    }
                }
            }
        }
    }

    /**
     * 判断每个座位对应站区间是否能选
     *
     * @param trainSeat
     * @param start
     * @param end
     * @return
     */
    public boolean canSell(DailyTrainSeat trainSeat, Integer start, Integer end) {
        String sell = trainSeat.getSell();
        String region = sell.substring(start, end);
        if (Integer.parseInt(region) >= 1) {
            log.info("该座位{}在此区间已售卖{}", trainSeat.getCarriageSeatIndex(), sell);
            return false;
        } else {
            region = region.replace('0', '1');
            sell = sell.substring(0, start) + region + sell.substring(end);
            trainSeat.setSell(sell);
            log.info("该座位{}可选，选座后该座位的售卖情况{}", trainSeat.getCarriageSeatIndex(), sell);
            return true;
        }
    }


    /**
     * 预扣减库存
     *
     * @param confirmOrder
     * @param tickets
     * @param dailyTrainTicket
     */
    private static void reduceTickets(ConfirmOrder confirmOrder, List<Ticket> tickets, DailyTrainTicket dailyTrainTicket) {
        for (Ticket ticket : tickets) {
            String seatTypeCode = ticket.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            //预扣减余票
            switch (seatTypeEnum) {
                case YDZ: {
                    Integer ydz = dailyTrainTicket.getYdz();
                    if (ydz <= 0) {
                        log.error("一等座余票不足");
                        confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                        throw new BusinessException(BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR);
                    }
                    dailyTrainTicket.setYdz(ydz - 1);
                    break;
                }
                case EDZ: {
                    Integer edz = dailyTrainTicket.getEdz();
                    if (edz <= 0) {
                        log.error("二等座余票不足");
                        confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                        throw new BusinessException(BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR);
                    }
                    dailyTrainTicket.setYdz(edz - 1);
                    break;
                }
                case RW: {
                    Integer rw = dailyTrainTicket.getRw();
                    if (rw <= 0) {
                        log.error("软卧余票不足");
                        confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                        throw new BusinessException(BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR);
                    }
                    dailyTrainTicket.setYdz(rw - 1);
                    break;
                }
                case YW: {
                    Integer yw = dailyTrainTicket.getYw();
                    if (yw <= 0) {
                        log.error("硬卧余票不足");
                        confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                        throw new BusinessException(BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR);
                    }
                    dailyTrainTicket.setYdz(yw - 1);
                    break;
                }
            }
        }
    }


}
