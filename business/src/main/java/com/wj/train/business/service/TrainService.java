package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.*;
import com.wj.train.business.enums.SeatColEnum;
import com.wj.train.business.mapper.TrainCarriageMapper;
import com.wj.train.business.mapper.TrainSeatMapper;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import com.wj.train.business.mapper.TrainMapper;
import com.wj.train.business.req.TrainQueryReq;
import com.wj.train.business.req.TrainSaveReq;
import com.wj.train.business.resp.TrainQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_CODE_UNIQUE_ERROR;

@Service
public class TrainService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

    @Resource
    private TrainMapper trainMapper;

    @Resource
    private TrainCarriageMapper trainCarriageMapper;

    @Resource
    private TrainSeatMapper trainSeatMapper;

    public void save(TrainSaveReq req) {
        Train trainByCode = getTrainByCode(req.getCode());
        if (trainByCode != null) {
            //车次已存在
            throw new BusinessException(BUSINESS_TRAIN_CODE_UNIQUE_ERROR);
        }
        DateTime now = DateTime.now();
        Train train = BeanUtil.copyProperties(req, Train.class);
        if (ObjectUtil.isNull(train.getId())) {
            train.setId(SnowFlowUtil.getSnowFlowId());
            train.setCreateTime(now);
            train.setUpdateTime(now);
            trainMapper.insert(train);
        } else {
            train.setUpdateTime(now);
            trainMapper.updateByPrimaryKey(train);
        }
    }

    /**
     * 根据车次查询
     *
     * @param code
     * @return
     */
    private Train getTrainByCode(String code) {
        TrainExample trainExample = new TrainExample();
        TrainExample.Criteria criteria = trainExample.createCriteria();
        criteria.andCodeEqualTo(code);
        List<Train> trains = trainMapper.selectByExample(trainExample);
        return trains.isEmpty() ? null : trains.get(0);
    }


    public PageResp<TrainQueryResp> queryList(TrainQueryReq req) {
        TrainExample trainExample = new TrainExample();
        trainExample.setOrderByClause("id asc");
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Train> trainList = trainMapper.selectByExample(trainExample);

        PageInfo<Train> pageInfo = new PageInfo<>(trainList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainQueryResp> list = BeanUtil.copyToList(trainList, TrainQueryResp.class);

        PageResp<TrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }


    public void delete(Long id) {
        trainMapper.deleteByPrimaryKey(id);
    }


    /**
     * 查询所有车次
     *
     * @param
     * @return
     */
    public List<TrainQueryResp> queryAll() {
        TrainExample trainExample = new TrainExample();
        trainExample.setOrderByClause("code asc");
        List<Train> trainList = trainMapper.selectByExample(trainExample);
        return BeanUtil.copyToList(trainList, TrainQueryResp.class);
    }


    /**
     * 一键生成所有的座位
     */
    @Transactional
    public void genSeats(String trainCode) {
        //如果原先有对应座位先删除
        deleteSeatByTrainCode(trainCode);
        //1. 根据车次查询对应的所有车厢
        List<TrainCarriage> carriageByTrainCode = getCarriageByTrainCode(trainCode);
        int count = 1;
        DateTime now = DateTime.now();
        //2. 遍历每一个车厢生成座位
        for (TrainCarriage trainCarriage : carriageByTrainCode) {
            for (int row = 1; row <= trainCarriage.getRowCount(); row++) {
                for (SeatColEnum seatColEnum : SeatColEnum.getColsByType(trainCarriage.getSeatType())) {
                    TrainSeat trainSeat = new TrainSeat();
                    trainSeat.setId(SnowFlowUtil.getSnowFlowId());
                    trainSeat.setTrainCode(trainCode);
                    trainSeat.setCarriageIndex(trainCarriage.getIndex());
                    if (row < 10) {
                        trainSeat.setRow("0" + row);
                    } else {
                        trainSeat.setRow(String.valueOf(row));
                    }
                    trainSeat.setCol(seatColEnum.getCode());
                    trainSeat.setSeatType(trainCarriage.getSeatType());
                    trainSeat.setCarriageSeatIndex(count++);
                    trainSeat.setCreateTime(now);
                    trainSeat.setUpdateTime(now);
                    trainSeatMapper.insert(trainSeat);
                }
            }
        }
    }

    private void deleteSeatByTrainCode(String trainCode) {
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        List<TrainSeat> trainSeats = trainSeatMapper.selectByExample(trainSeatExample);
        if (!trainSeats.isEmpty()) {
            trainSeatMapper.deleteByExample(trainSeatExample);
        }
    }

    private List<TrainCarriage> getCarriageByTrainCode(String trainCode) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        return trainCarriageMapper.selectByExample(trainCarriageExample);
    }


}
