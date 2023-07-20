package com.wj.train.member.service;

import com.wj.train.common.resp.PageResp;
import com.wj.train.member.req.PassengerAddReq;
import com.wj.train.member.req.PassengerQueryReq;
import com.wj.train.member.resp.PassengerQueryResp;

import java.util.List;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description
 */
public interface PassengerService {

    void addPassenger(PassengerAddReq passengerAddReq);

    PageResp<PassengerQueryResp> queryPassengers(PassengerQueryReq passengerQueryReq);

    void deletePassenger(Long id);

    List<PassengerQueryResp> queryMyPassengers(Long memberId);
}
