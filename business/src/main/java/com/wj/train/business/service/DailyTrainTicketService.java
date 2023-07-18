package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.DailyTrainTicket;
import com.wj.train.business.domain.DailyTrainTicketExample;
import com.wj.train.business.domain.TrainStation;
import com.wj.train.business.enums.SeatTypeEnum;
import com.wj.train.business.enums.TrainTypeEnum;
import com.wj.train.business.mapper.DailyTrainTicketMapper;
import com.wj.train.business.req.DailyTrainTicketQueryReq;
import com.wj.train.business.req.DailyTrainTicketSaveReq;
import com.wj.train.business.resp.DailyTrainTicketQueryResp;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

@Service
public class DailyTrainTicketService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private TrainStationService trainStationService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    public void save(DailyTrainTicketSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(req, DailyTrainTicket.class);
        if (ObjectUtil.isNull(dailyTrainTicket.getId())) {
            dailyTrainTicket.setId(SnowFlowUtil.getSnowFlowId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        } else {
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.updateByPrimaryKey(dailyTrainTicket);
        }
    }

    public PageResp<DailyTrainTicketQueryResp> queryList(DailyTrainTicketQueryReq req) {
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.setOrderByClause("start_index asc,end_index desc");
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainTicket> dailyTrainTicketList = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);

        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainTicketList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainTicketQueryResp> list = BeanUtil.copyToList(dailyTrainTicketList, DailyTrainTicketQueryResp.class);

        PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }

    public void genDailyTickets(String trainCode, Date date, String type) {
        int ydz = dailyTrainSeatService.getTicketNum(trainCode, date, SeatTypeEnum.YDZ.getCode());
        int edz = dailyTrainSeatService.getTicketNum(trainCode, date, SeatTypeEnum.EDZ.getCode());
        int rw = dailyTrainSeatService.getTicketNum(trainCode, date, SeatTypeEnum.RW.getCode());
        int yw = dailyTrainSeatService.getTicketNum(trainCode, date, SeatTypeEnum.YW.getCode());
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        dailyTrainTicketMapper.deleteByExample(dailyTrainTicketExample);
        List<TrainStation> stationsByTrainCode = trainStationService.getStationsByTrainCode(trainCode);
        DateTime now = DateTime.now();
        for (int i = 0; i < stationsByTrainCode.size(); i++) {
            TrainStation trainStationStart = stationsByTrainCode.get(i);
            BigDecimal distace = BigDecimal.ZERO;
            for (int j = i + 1; j < stationsByTrainCode.size(); j++) {
                TrainStation trainStationEnd = stationsByTrainCode.get(j);
                distace = distace.add(trainStationEnd.getKm());
                DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
                dailyTrainTicket.setId(SnowFlowUtil.getSnowFlowId());
                dailyTrainTicket.setDate(date);
                dailyTrainTicket.setTrainCode(trainCode);
                dailyTrainTicket.setStart(trainStationStart.getName());
                dailyTrainTicket.setStartPinyin(trainStationStart.getNamePinyin());
                dailyTrainTicket.setStartTime(trainStationStart.getOutTime());
                dailyTrainTicket.setStartIndex(trainStationStart.getIndex());
                dailyTrainTicket.setEnd(trainStationEnd.getName());
                dailyTrainTicket.setEndPinyin(trainStationEnd.getNamePinyin());
                dailyTrainTicket.setEndTime(trainStationEnd.getStopTime());
                dailyTrainTicket.setEndIndex(trainStationEnd.getIndex());
                dailyTrainTicket.setYdz(ydz);
                dailyTrainTicket.setYdzPrice(distace.multiply(getTrainPrice(type)).multiply(SeatTypeEnum.YDZ.getPrice()).setScale(2, RoundingMode.HALF_UP));
                dailyTrainTicket.setEdz(edz);
                dailyTrainTicket.setEdzPrice(distace.multiply(getTrainPrice(type)).multiply(SeatTypeEnum.EDZ.getPrice()).setScale(2, RoundingMode.HALF_UP));
                dailyTrainTicket.setRw(rw);
                dailyTrainTicket.setRwPrice(distace.multiply(getTrainPrice(type)).multiply(SeatTypeEnum.RW.getPrice()).setScale(2, RoundingMode.HALF_UP));
                dailyTrainTicket.setYw(yw);
                dailyTrainTicket.setYwPrice(distace.multiply(getTrainPrice(type)).multiply(SeatTypeEnum.YW.getPrice()).setScale(2, RoundingMode.HALF_UP));
                dailyTrainTicket.setCreateTime(now);
                dailyTrainTicket.setUpdateTime(now);
                dailyTrainTicketMapper.insert(dailyTrainTicket);
            }
        }
    }

    public BigDecimal getTrainPrice(String type) {
        EnumSet<TrainTypeEnum> trainTypeEnums = EnumSet.allOf(TrainTypeEnum.class);
        for (TrainTypeEnum trainTypeEnum : trainTypeEnums) {
            if (trainTypeEnum.getCode().equals(type)) {
                return trainTypeEnum.getPriceRate();
            }
        }
        return null;
    }

}
