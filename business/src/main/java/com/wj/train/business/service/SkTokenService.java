package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.SkToken;
import com.wj.train.business.domain.SkTokenExample;
import com.wj.train.business.mapper.SkTokenMapper;
import com.wj.train.business.req.SkTokenQueryReq;
import com.wj.train.business.req.SkTokenSaveReq;
import com.wj.train.business.resp.SkTokenQueryResp;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SkTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(SkTokenService.class);

    @Resource
    private SkTokenMapper skTokenMapper;

    @Resource
    private TrainStationService trainStationService;

    @Resource
    private TrainCarriageService trainCarriageService;

    public void save(SkTokenSaveReq req) {
        DateTime now = DateTime.now();
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if (ObjectUtil.isNull(skToken.getId())) {
            skToken.setId(SnowFlowUtil.getSnowFlowId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        } else {
            skToken.setUpdateTime(now);
            skTokenMapper.updateByPrimaryKey(skToken);
        }
    }

    public PageResp<SkTokenQueryResp> queryList(SkTokenQueryReq req) {
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.setOrderByClause("id desc");
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<SkToken> skTokenList = skTokenMapper.selectByExample(skTokenExample);

        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokenList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<SkTokenQueryResp> list = BeanUtil.copyToList(skTokenList, SkTokenQueryResp.class);

        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        skTokenMapper.deleteByPrimaryKey(id);
    }

    /**
     * 生成每日车次的令牌
     *
     * @param trainCode
     * @param date
     */
    public void genSkToken(String trainCode, Date date) {
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        long count = skTokenMapper.countByExample(skTokenExample);
        if (count > 0) {
            //删除原有的车次令牌
            skTokenMapper.deleteByExample(skTokenExample);
        }
        SkToken skToken = new SkToken();
        skToken.setId(SnowFlowUtil.getSnowFlowId());
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setCreateTime(DateTime.now());
        skToken.setUpdateTime(DateTime.now());
        int trainStationsNums = trainStationService.getStationsByTrainCode(trainCode).size();
        Integer totalSeatsNums = trainCarriageService.getTotalSeatsNums(trainCode);
        Integer seatCount = (int) ((trainStationsNums - 1) * totalSeatsNums * 0.75);
        skToken.setCount(seatCount);
        skTokenMapper.insert(skToken);
    }
}
