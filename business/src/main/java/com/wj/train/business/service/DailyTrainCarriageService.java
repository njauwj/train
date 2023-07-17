package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.DailyTrainCarriage;
import com.wj.train.business.domain.DailyTrainCarriageExample;
import com.wj.train.business.domain.Train;
import com.wj.train.business.domain.TrainCarriage;
import com.wj.train.business.mapper.DailyTrainCarriageMapper;
import com.wj.train.business.req.DailyTrainCarriageQueryReq;
import com.wj.train.business.req.DailyTrainCarriageSaveReq;
import com.wj.train.business.resp.DailyTrainCarriageQueryResp;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR;

@Service
@Slf4j
public class DailyTrainCarriageService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainCarriageService.class);

    @Resource
    private DailyTrainCarriageMapper dailyTrainCarriageMapper;

    @Resource
    private TrainCarriageService trainCarriageService;

    public void save(DailyTrainCarriageSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(req, DailyTrainCarriage.class);
        if (ObjectUtil.isNull(dailyTrainCarriage.getId())) {
            DailyTrainCarriage dailyTrainCarriageByIndex = getDailyTrainCarriageByIndex(req.getTrainCode(), req.getDate(), req.getIndex());
            if (dailyTrainCarriageByIndex != null) {
                log.error("同一天车次车厢已存在");
                throw new BusinessException(BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR);
            }
            dailyTrainCarriage.setId(SnowFlowUtil.getSnowFlowId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        } else {
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.updateByPrimaryKey(dailyTrainCarriage);
        }
    }

    /**
     * 根据箱序查询
     *
     * @param trainCode
     * @param date
     * @param index
     * @return
     */
    private DailyTrainCarriage getDailyTrainCarriageByIndex(String trainCode, Date date, Integer index) {
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        criteria.andDateEqualTo(date);
        criteria.andIndexEqualTo(index);
        List<DailyTrainCarriage> dailyTrainCarriages = dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);
        return dailyTrainCarriages.isEmpty() ? null : dailyTrainCarriages.get(0);
    }


    public PageResp<DailyTrainCarriageQueryResp> queryList(DailyTrainCarriageQueryReq req) {
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.setOrderByClause("date desc,train_code asc,`index` asc");
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        String code = req.getTrainCode();
        if (CharSequenceUtil.isNotBlank(code)) {
            criteria.andTrainCodeEqualTo(code);
        }
        Date date = req.getDate();
        if (date != null) {
            criteria.andDateEqualTo(date);
        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainCarriage> dailyTrainCarriageList = dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);

        PageInfo<DailyTrainCarriage> pageInfo = new PageInfo<>(dailyTrainCarriageList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainCarriageQueryResp> list = BeanUtil.copyToList(dailyTrainCarriageList, DailyTrainCarriageQueryResp.class);

        PageResp<DailyTrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainCarriageMapper.deleteByPrimaryKey(id);
    }


    public void genDailyCarriages(Train train, Date date) {
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.createCriteria().andTrainCodeEqualTo(train.getCode());
        dailyTrainCarriageMapper.deleteByExample(dailyTrainCarriageExample);
        List<TrainCarriage> carriagesByTrainCode = trainCarriageService.getCarriagesByTrainCode(train.getCode());
        for (TrainCarriage trainCarriage : carriagesByTrainCode) {
            genDailyCarriage(trainCarriage, date);
        }
    }

    public void genDailyCarriage(TrainCarriage trainCarriage, Date date) {
        DateTime now = DateTime.now();
        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(trainCarriage, DailyTrainCarriage.class);
        dailyTrainCarriage.setId(SnowFlowUtil.getSnowFlowId());
        dailyTrainCarriage.setDate(date);
        dailyTrainCarriage.setCreateTime(now);
        dailyTrainCarriage.setUpdateTime(now);
        dailyTrainCarriageMapper.insert(dailyTrainCarriage);
    }

}
