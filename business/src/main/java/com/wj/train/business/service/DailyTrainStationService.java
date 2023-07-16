package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.DailyTrainStation;
import com.wj.train.business.domain.DailyTrainStationExample;
import com.wj.train.business.mapper.DailyTrainStationMapper;
import com.wj.train.business.req.DailyTrainStationQueryReq;
import com.wj.train.business.req.DailyTrainStationSaveReq;
import com.wj.train.business.resp.DailyTrainStationQueryResp;
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

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR;
import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR;

@Service
@Slf4j
public class DailyTrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationService.class);

    @Resource
    private DailyTrainStationMapper dailyTrainStationMapper;

    public void save(DailyTrainStationSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(req, DailyTrainStation.class);
        if (ObjectUtil.isNull(dailyTrainStation.getId())) {
            DailyTrainStation dailyTrainByIndex = getDailyTrainStationByIndex(req.getTrainCode(), req.getDate(), req.getIndex());
            if (dailyTrainByIndex != null) {
                log.error("同一天车次站序不能重复");
                throw new BusinessException(BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
            }
            DailyTrainStation dailyTrainByName = getDailyTrainStationByName(req.getTrainCode(), req.getDate(), req.getName());
            if (dailyTrainByName != null) {
                log.error("同一天车次站名不能重复");
                throw new BusinessException(BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);
            }
            dailyTrainStation.setId(SnowFlowUtil.getSnowFlowId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        } else {
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.updateByPrimaryKey(dailyTrainStation);
        }
    }

    /**
     * 通过站序查找
     *
     * @param trainCode
     * @param date
     * @param index
     * @return
     */
    private DailyTrainStation getDailyTrainStationByIndex(String trainCode, Date date, Integer index) {
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        criteria.andDateEqualTo(date);
        criteria.andTrainCodeEqualTo(trainCode);
        criteria.andIndexEqualTo(index);
        List<DailyTrainStation> dailyTrainStations = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
        return dailyTrainStations.isEmpty() ? null : dailyTrainStations.get(0);
    }

    /**
     * 通过站名查找
     *
     * @param trainCode
     * @param date
     * @param name
     * @return
     */
    private DailyTrainStation getDailyTrainStationByName(String trainCode, Date date, String name) {
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        criteria.andDateEqualTo(date);
        criteria.andTrainCodeEqualTo(trainCode);
        criteria.andNameEqualTo(name);
        List<DailyTrainStation> dailyTrainStations = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
        return dailyTrainStations.isEmpty() ? null : dailyTrainStations.get(0);
    }


    public PageResp<DailyTrainStationQueryResp> queryList(DailyTrainStationQueryReq req) {
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.setOrderByClause("date desc,train_code asc,`index` asc");
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
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
        List<DailyTrainStation> dailyTrainStationList = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);

        PageInfo<DailyTrainStation> pageInfo = new PageInfo<>(dailyTrainStationList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainStationQueryResp> list = BeanUtil.copyToList(dailyTrainStationList, DailyTrainStationQueryResp.class);

        PageResp<DailyTrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainStationMapper.deleteByPrimaryKey(id);
    }
}
