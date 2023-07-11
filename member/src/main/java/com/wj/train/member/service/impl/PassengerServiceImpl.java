package com.wj.train.member.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.github.pagehelper.PageHelper;
import com.wj.train.common.context.LocalContext;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import com.wj.train.member.domain.Passenger;
import com.wj.train.member.domain.PassengerExample;
import com.wj.train.member.mapper.PassengerMapper;
import com.wj.train.member.req.PassengerAddReq;
import com.wj.train.member.req.PassengerQueryReq;
import com.wj.train.member.resp.PassengerQueryResp;
import com.wj.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description
 */
@Service
public class PassengerServiceImpl implements PassengerService {


    @Resource
    private PassengerMapper passengerMapper;


    /**
     * 添加乘客
     *
     * @param passengerAddReq
     */
    public void addPassenger(PassengerAddReq passengerAddReq) {
        Long memberId = LocalContext.get().getId();
        DateTime now = DateTime.now();
        passengerAddReq.setId(SnowFlowUtil.getSnowFlowId());
        passengerAddReq.setMemberId(memberId);
        passengerAddReq.setCreateTime(now);
        passengerAddReq.setUpdateTime(now);
        Passenger passenger = new Passenger();
        BeanUtils.copyProperties(passengerAddReq, passenger);
        passengerMapper.insert(passenger);
    }

    /**
     * 查询乘客
     *
     * @param passengerQueryReq
     * @return
     */
    @Override
    public PageResp<PassengerQueryResp> queryPassengers(PassengerQueryReq passengerQueryReq) {
        Integer page = passengerQueryReq.getPage();
        Integer size = passengerQueryReq.getSize();
        Long memberId = passengerQueryReq.getMemberId();
        PassengerExample passengerExample = new PassengerExample();
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        if (memberId != null) {
            criteria.andMemberIdEqualTo(memberId);
        }
        PageHelper.startPage(page, size);
        List<Passenger> passengers = passengerMapper.selectByExample(passengerExample);
        List<PassengerQueryResp> passengerQueryResps = BeanUtil.copyToList(passengers, PassengerQueryResp.class);
        return new PageResp<>(passengerQueryResps.size(), passengerQueryResps);
    }


}