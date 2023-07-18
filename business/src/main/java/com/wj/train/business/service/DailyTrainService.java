package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.DailyTrain;
import com.wj.train.business.domain.DailyTrainExample;
import com.wj.train.business.domain.Train;
import com.wj.train.business.mapper.DailyTrainMapper;
import com.wj.train.business.req.DailyTrainQueryReq;
import com.wj.train.business.req.DailyTrainSaveReq;
import com.wj.train.business.resp.DailyTrainQueryResp;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_CODE_UNIQUE_ERROR;

@Service
@Slf4j
public class DailyTrainService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainService.class);

    @Resource
    private DailyTrainMapper dailyTrainMapper;

    @Resource
    private TrainService trainService;

    @Resource
    private DailyTrainStationService dailyTrainStationService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    public void save(DailyTrainSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(req, DailyTrain.class);
        if (ObjectUtil.isNull(dailyTrain.getId())) {
            DailyTrain dailyTrainByUniqueKey = getDailyTrainByUniqueKey(req.getCode(), req.getDate());
            if (dailyTrainByUniqueKey != null) {
                log.error("同一天车次不能重复");
                throw new BusinessException(BUSINESS_TRAIN_CODE_UNIQUE_ERROR);
            }
            dailyTrain.setId(SnowFlowUtil.getSnowFlowId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insert(dailyTrain);
        } else {
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.updateByPrimaryKey(dailyTrain);
        }
    }

    /**
     * 获取每日车次
     *
     * @param code
     * @param date
     * @return
     */
    private DailyTrain getDailyTrainByUniqueKey(String code, Date date) {
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();
        criteria.andCodeEqualTo(code);
        criteria.andDateEqualTo(date);
        List<DailyTrain> dailyTrains = dailyTrainMapper.selectByExample(dailyTrainExample);
        return dailyTrains.isEmpty() ? null : dailyTrains.get(0);
    }

    public PageResp<DailyTrainQueryResp> queryList(DailyTrainQueryReq req) {
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.setOrderByClause("date desc,code asc");
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();
        String code = req.getCode();
        if (StrUtil.isNotBlank(code)) {
            criteria.andCodeEqualTo(code);
        }
        Date date = req.getDate();
        if (date != null) {
            criteria.andDateEqualTo(date);
        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrain> dailyTrainList = dailyTrainMapper.selectByExample(dailyTrainExample);

        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrainList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainQueryResp> list = BeanUtil.copyToList(dailyTrainList, DailyTrainQueryResp.class);

        PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据基本数据生成每日车次数据
     *
     * @param date
     */
    @Transactional
    public void genDaily(Date date) {
        List<Train> trains = trainService.getAllTrains();
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.createCriteria().andDateEqualTo(date);
        //先删除原有的每日车次数据
        dailyTrainMapper.deleteByExample(dailyTrainExample);
        for (Train train : trains) {
            log.info("开始生成{}的车次数据", date);
            genDailyTrain(train, date);
            log.info("开始生成{}的车次{}的车站数据", date, train.getCode());
            dailyTrainStationService.genDailyStations(train, date);
            log.info("开始生成{}的车次{}的车厢数据", date, train.getCode());
            dailyTrainCarriageService.genDailyCarriages(train, date);
            log.info("开始生成{}的车次{}的座位数据", date, train.getCode());
            dailyTrainSeatService.genDailySeats(train, date);
            log.info("开始生成{}的车次{}的座位票数数据", date, train.getCode());
            dailyTrainTicketService.genDailyTickets(train.getCode(), date, train.getType());
        }
    }

    private void genDailyTrain(Train train, Date date) {
        DateTime now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
        dailyTrain.setId(SnowFlowUtil.getSnowFlowId());
        dailyTrain.setDate(date);
        dailyTrain.setCreateTime(now);
        dailyTrain.setUpdateTime(now);
        dailyTrainMapper.insert(dailyTrain);
    }
}
