package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import com.wj.train.business.domain.Station;
import com.wj.train.business.domain.StationExample;
import com.wj.train.business.mapper.StationMapper;
import com.wj.train.business.req.StationQueryReq;
import com.wj.train.business.req.StationSaveReq;
import com.wj.train.business.resp.StationQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_STATION_NAME_UNIQUE_ERROR;

@Service
public class StationService {

    private static final Logger LOG = LoggerFactory.getLogger(StationService.class);

    @Resource
    private StationMapper stationMapper;

    public void save(StationSaveReq req) {
        Station stationDB = getStationByName(req.getName());
        if (stationDB != null) {
            //车站站名是唯一的不允许重复添加
            throw new BusinessException(BUSINESS_STATION_NAME_UNIQUE_ERROR);
        }
        DateTime now = DateTime.now();
        Station station = BeanUtil.copyProperties(req, Station.class);
        if (ObjectUtil.isNull(station.getId())) {
            station.setId(SnowFlowUtil.getSnowFlowId());
            station.setCreateTime(now);
            station.setUpdateTime(now);
            stationMapper.insert(station);
        } else {
            station.setUpdateTime(now);
            stationMapper.updateByPrimaryKey(station);
        }
    }

    private Station getStationByName(String name) {
        StationExample stationExample = new StationExample();
        StationExample.Criteria criteria = stationExample.createCriteria();
        criteria.andNameEqualTo(name);
        List<Station> stations = stationMapper.selectByExample(stationExample);
        return stations.isEmpty() ? null : stations.get(0);
    }

    /**
     * 分页查询所有站点
     *
     * @param req
     * @return
     */
    public PageResp<StationQueryResp> queryList(StationQueryReq req) {
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("name asc");
        StationExample.Criteria criteria = stationExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Station> stationList = stationMapper.selectByExample(stationExample);

        PageInfo<Station> pageInfo = new PageInfo<>(stationList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<StationQueryResp> list = BeanUtil.copyToList(stationList, StationQueryResp.class);

        PageResp<StationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        stationMapper.deleteByPrimaryKey(id);
    }

    /**
     * 查询所有的站点
     *
     * @return
     */
    public List<StationQueryResp> queryAll() {
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("id desc");
        List<Station> stationList = stationMapper.selectByExample(stationExample);
        return BeanUtil.copyToList(stationList, StationQueryResp.class);
    }
}
