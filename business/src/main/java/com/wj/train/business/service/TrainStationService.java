package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import com.wj.train.business.domain.TrainStation;
import com.wj.train.business.domain.TrainStationExample;
import com.wj.train.business.mapper.TrainStationMapper;
import com.wj.train.business.req.TrainStationQueryReq;
import com.wj.train.business.req.TrainStationSaveReq;
import com.wj.train.business.resp.TrainStationQueryResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR;
import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR;

@Service
@Slf4j
public class TrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainStationService.class);

    @Resource
    private TrainStationMapper trainStationMapper;

    public void save(TrainStationSaveReq req) {
        TrainStation trainStationByIndex = getTrainStationByIndex(req.getTrainCode(), req.getIndex());
        if (trainStationByIndex != null) {
            log.error("同车次站序已存在");
            throw new BusinessException(BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
        }
        TrainStation trainStationByName = getTrainStationByName(req.getTrainCode(), req.getName());
        if (trainStationByName != null) {
            log.error("同车次站名已存在");
            throw new BusinessException(BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);
        }
        DateTime now = DateTime.now();
        TrainStation trainStation = BeanUtil.copyProperties(req, TrainStation.class);
        if (ObjectUtil.isNull(trainStation.getId())) {
            trainStation.setId(SnowFlowUtil.getSnowFlowId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        } else {
            trainStation.setUpdateTime(now);
            trainStationMapper.updateByPrimaryKey(trainStation);
        }
    }

    /**
     * 根据车次，站序查询
     *
     * @param trainCode
     * @param index
     * @return
     */
    private TrainStation getTrainStationByIndex(String trainCode, Integer index) {
        TrainStationExample trainStationExample = new TrainStationExample();
        TrainStationExample.Criteria criteria = trainStationExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        criteria.andIndexEqualTo(index);
        List<TrainStation> trainStations = trainStationMapper.selectByExample(trainStationExample);
        return trainStations.isEmpty() ? null : trainStations.get(0);
    }

    /**
     * 根据车次，站名查询
     *
     * @param trainCode
     * @param name
     * @return
     */
    private TrainStation getTrainStationByName(String trainCode, String name) {
        TrainStationExample trainStationExample = new TrainStationExample();
        TrainStationExample.Criteria criteria = trainStationExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        criteria.andNameEqualTo(name);
        List<TrainStation> trainStations = trainStationMapper.selectByExample(trainStationExample);
        return trainStations.isEmpty() ? null : trainStations.get(0);
    }


    /**
     * 分页查询车次的车站信息
     *
     * @param req
     * @return
     */
    public PageResp<TrainStationQueryResp> queryList(TrainStationQueryReq req) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.setOrderByClause("train_code asc,`index` asc");
        TrainStationExample.Criteria criteria = trainStationExample.createCriteria();
        String trainCode = req.getTrainCode();
        if (CharSequenceUtil.isNotBlank(trainCode)) {
            //按照车次查询所有途径车站信息
            criteria.andTrainCodeEqualTo(trainCode);
        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainStation> trainStationList = trainStationMapper.selectByExample(trainStationExample);

        PageInfo<TrainStation> pageInfo = new PageInfo<>(trainStationList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainStationQueryResp> list = BeanUtil.copyToList(trainStationList, TrainStationQueryResp.class);

        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainStationMapper.deleteByPrimaryKey(id);
    }
}
