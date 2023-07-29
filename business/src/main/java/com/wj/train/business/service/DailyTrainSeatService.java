package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.*;
import com.wj.train.business.mapper.DailyTrainSeatMapper;
import com.wj.train.business.req.DailyTrainSeatQueryReq;
import com.wj.train.business.req.DailyTrainSeatSaveReq;
import com.wj.train.business.req.SeatSellReq;
import com.wj.train.business.req.SeatSellResp;
import com.wj.train.business.resp.DailyTrainSeatQueryResp;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainSeatService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainSeatService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private TrainSeatService trainSeatService;

    @Resource
    private TrainStationService trainStationService;

    public void save(DailyTrainSeatSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(req, DailyTrainSeat.class);
        if (ObjectUtil.isNull(dailyTrainSeat.getId())) {
            dailyTrainSeat.setId(SnowFlowUtil.getSnowFlowId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        } else {
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKey(dailyTrainSeat);
        }
    }

    public PageResp<DailyTrainSeatQueryResp> queryList(DailyTrainSeatQueryReq req) {
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.setOrderByClause("date desc,train_code asc,carriage_index asc,carriage_seat_index asc");
        DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();
        String trainCode = req.getTrainCode();
        if (CharSequenceUtil.isNotBlank(trainCode)) {
            criteria.andTrainCodeEqualTo(trainCode);
        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainSeat> dailyTrainSeatList = dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);

        PageInfo<DailyTrainSeat> pageInfo = new PageInfo<>(dailyTrainSeatList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainSeatQueryResp> list = BeanUtil.copyToList(dailyTrainSeatList, DailyTrainSeatQueryResp.class);

        PageResp<DailyTrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainSeatMapper.deleteByPrimaryKey(id);
    }


    public void genDailySeats(Train train, Date date) {
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.createCriteria().andTrainCodeEqualTo(train.getCode()).andDateEqualTo(date);
        dailyTrainSeatMapper.deleteByExample(dailyTrainSeatExample);
        List<TrainStation> stationsByTrainCode = trainStationService.getStationsByTrainCode(train.getCode());
        int size = stationsByTrainCode.size() - 1;
        List<TrainSeat> seatsByTrainCode = trainSeatService.getSeatsByTrainCode(train.getCode());
        for (TrainSeat trainSeat : seatsByTrainCode) {
            genDailySeat(trainSeat, date, size);
        }
    }

    public void genDailySeat(TrainSeat trainSeat, Date date, int size) {
        DateTime now = DateTime.now();
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(trainSeat, DailyTrainSeat.class);
        dailyTrainSeat.setId(SnowFlowUtil.getSnowFlowId());
        dailyTrainSeat.setDate(date);
        dailyTrainSeat.setSell(StrUtil.fillBefore("", '0', size));
        dailyTrainSeat.setCreateTime(now);
        dailyTrainSeat.setUpdateTime(now);
        dailyTrainSeatMapper.insert(dailyTrainSeat);
    }

    /**
     * 获取座位票数
     *
     * @param trainCode
     * @param date
     * @param code      座位类型
     * @return
     */
    public int getTicketNum(String trainCode, Date date, String code) {
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode).andSeatTypeEqualTo(code);
        long count = dailyTrainSeatMapper.countByExample(dailyTrainSeatExample);
        return count == 0 ? -1 : (int) count;
    }


    /**
     * 根据车厢查询所有座位
     *
     * @param trainCode
     * @param date
     * @param carriageIndex
     * @return
     */
    public List<DailyTrainSeat> getSeatsByCarriageIndex(String trainCode, Date date, Integer carriageIndex) {
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.setOrderByClause("carriage_seat_index asc");
        dailyTrainSeatExample.createCriteria().andTrainCodeEqualTo(trainCode).andDateEqualTo(date).andCarriageIndexEqualTo(carriageIndex);
        return dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);
    }

    /**
     * 座位售卖数据，用来展示座位售卖详情
     *
     * @param seatSellReq
     * @return
     */
    public List<SeatSellResp> seatSellCondition(SeatSellReq seatSellReq) {
        Date date = seatSellReq.getDate();
        String trainCode = seatSellReq.getTrainCode();
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.setOrderByClause("carriage_index asc,carriage_seat_index asc");
        dailyTrainSeatExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        List<DailyTrainSeat> dailyTrainSeats = dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);
        return BeanUtil.copyToList(dailyTrainSeats, SeatSellResp.class);
    }
}
