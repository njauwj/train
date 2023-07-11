package com.wj.train.member.service.impl;

import cn.hutool.core.date.DateTime;
import com.wj.train.common.context.LocalContext;
import com.wj.train.common.utils.SnowFlowUtil;
import com.wj.train.member.domain.Passenger;
import com.wj.train.member.mapper.PassengerMapper;
import com.wj.train.member.req.PassengerAddReq;
import com.wj.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description
 */
@Service
public class PassengerServiceImpl implements PassengerService {


    @Resource
    private PassengerMapper passengerMapper;


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


}
