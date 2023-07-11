package com.wj.train.member.controller;

import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.utils.RespUtil;
import com.wj.train.member.req.PassengerAddReq;
import com.wj.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
@RestController
@RequestMapping("/passenger")
public class PassengerController {

    @Resource
    private PassengerService passengerService;


    @PostMapping("/save")
    public CommonResp<Object> sendCode(@Valid @RequestBody PassengerAddReq passengerAddReq) {
        passengerService.addPassenger(passengerAddReq);
        return RespUtil.success(true);
    }

}
