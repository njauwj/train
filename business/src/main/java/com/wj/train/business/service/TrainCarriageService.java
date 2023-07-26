package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.TrainCarriage;
import com.wj.train.business.domain.TrainCarriageExample;
import com.wj.train.business.enums.SeatColEnum;
import com.wj.train.business.mapper.TrainCarriageMapper;
import com.wj.train.business.req.TrainCarriageQueryReq;
import com.wj.train.business.req.TrainCarriageSaveReq;
import com.wj.train.business.resp.TrainCarriageQueryResp;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR;

@Service
@Slf4j
public class TrainCarriageService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainCarriageService.class);

    @Resource
    private TrainCarriageMapper trainCarriageMapper;

    /**
     * 新增车厢或者更新车厢信息
     *
     * @param req
     */
    public void save(TrainCarriageSaveReq req) {
        DateTime now = DateTime.now();
        TrainCarriage trainCarriage = BeanUtil.copyProperties(req, TrainCarriage.class);
        String seatType = trainCarriage.getSeatType();
        Integer rowCount = trainCarriage.getRowCount();
        int col = SeatColEnum.getColsByType(seatType).size();
        if (ObjectUtil.isNull(trainCarriage.getId())) {
            TrainCarriage trainCarriageByIndex = getTrainCarriageByIndex(req.getTrainCode(), req.getIndex());
            if (trainCarriageByIndex != null) {
                log.error("同车次车厢已存在");
                throw new BusinessException(BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR);
            }
            //新增车厢信息
            trainCarriage.setId(SnowFlowUtil.getSnowFlowId());
            trainCarriage.setCreateTime(now);
            trainCarriage.setUpdateTime(now);
            trainCarriage.setSeatCount(rowCount * col);
            trainCarriage.setColCount(col);
            trainCarriageMapper.insert(trainCarriage);
        } else {
            //更新车厢信息
            trainCarriage.setUpdateTime(now);
            trainCarriage.setSeatCount(rowCount * col);
            trainCarriage.setColCount(col);
            trainCarriageMapper.updateByPrimaryKey(trainCarriage);
        }
    }

    /**
     * 根据车次，车厢查询
     *
     * @param trainCode
     * @param index
     * @return
     */
    private TrainCarriage getTrainCarriageByIndex(String trainCode, Integer index) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        criteria.andIndexEqualTo(index);
        List<TrainCarriage> trainCarriages = trainCarriageMapper.selectByExample(trainCarriageExample);
        return trainCarriages.isEmpty() ? null : trainCarriages.get(0);
    }

    public PageResp<TrainCarriageQueryResp> queryList(TrainCarriageQueryReq req) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();
        String trainCode = req.getTrainCode();
        if (CharSequenceUtil.isNotBlank(trainCode)) {
            criteria.andTrainCodeEqualTo(trainCode);
        }
        trainCarriageExample.setOrderByClause("train_code asc,`index` asc");
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainCarriage> trainCarriageList = trainCarriageMapper.selectByExample(trainCarriageExample);
        PageInfo<TrainCarriage> pageInfo = new PageInfo<>(trainCarriageList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());
        List<TrainCarriageQueryResp> list = BeanUtil.copyToList(trainCarriageList, TrainCarriageQueryResp.class);
        PageResp<TrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainCarriageMapper.deleteByPrimaryKey(id);
    }


    public List<TrainCarriage> getCarriagesByTrainCode(String trainCode) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainCarriageMapper.selectByExample(trainCarriageExample);
    }

    public Integer getTotalSeatsNums(String trainCode) {
        List<TrainCarriage> carriagesByTrainCode = getCarriagesByTrainCode(trainCode);
        int count = 0;
        for (TrainCarriage trainCarriage : carriagesByTrainCode) {
            count += trainCarriage.getSeatCount();
        }
        return count;
    }
}
