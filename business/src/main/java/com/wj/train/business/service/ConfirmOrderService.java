package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.ConfirmOrder;
import com.wj.train.business.domain.ConfirmOrderExample;
import com.wj.train.business.domain.DailyTrainTicket;
import com.wj.train.business.domain.Ticket;
import com.wj.train.business.enums.ConfirmOrderStatusEnum;
import com.wj.train.business.enums.SeatTypeEnum;
import com.wj.train.business.mapper.ConfirmOrderMapper;
import com.wj.train.business.mapper.DailyTrainSeatMapper;
import com.wj.train.business.mapper.DailyTrainTicketMapper;
import com.wj.train.business.req.ConfirmOrderQueryReq;
import com.wj.train.business.req.ConfirmOrderSaveReq;
import com.wj.train.business.resp.ConfirmOrderQueryResp;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR;

@Service
@Slf4j
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

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
        DateTime now = DateTime.now();
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
                        confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                        throw new BusinessException(BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR);
                    }
                    dailyTrainTicket.setYdz(ydz - 1);
                    break;
                }
                case EDZ: {
                    Integer edz = dailyTrainTicket.getEdz();
                    if (edz <= 0) {
                        confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                        throw new BusinessException(BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR);
                    }
                    dailyTrainTicket.setYdz(edz - 1);
                    break;
                }
                case RW: {
                    Integer rw = dailyTrainTicket.getRw();
                    if (rw <= 0) {
                        confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                        throw new BusinessException(BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR);
                    }
                    dailyTrainTicket.setYdz(rw - 1);
                    break;
                }
                case YW: {
                    Integer yw = dailyTrainTicket.getYw();
                    if (yw <= 0) {
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
