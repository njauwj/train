package com.wj.train.member.controller;

import com.wj.train.common.context.LocalContext;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.RespUtil;
import com.wj.train.member.req.PassengerAddReq;
import com.wj.train.member.req.PassengerQueryReq;
import com.wj.train.member.resp.PassengerQueryResp;
import com.wj.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public CommonResp<Object> addPassenger(@Valid @RequestBody PassengerAddReq passengerAddReq) {
        passengerService.addPassenger(passengerAddReq);
        return RespUtil.success(true);
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<PassengerQueryResp>> queryAllPassengers(@Valid PassengerQueryReq passengerQueryReq) {
        Long memberId = LocalContext.getMemberId();
        passengerQueryReq.setMemberId(memberId);
        PageResp<PassengerQueryResp> passengers = passengerService.queryPassengers(passengerQueryReq);
        return RespUtil.success(passengers);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> removePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return RespUtil.success(true);
    }

    @GetMapping("/query-mine")
    public CommonResp<List<PassengerQueryResp>> queryAllPassengers() {
        Long memberId = LocalContext.getMemberId();
        List<PassengerQueryResp> passengers = passengerService.queryMyPassengers(memberId);
        return RespUtil.success(passengers);
    }

}
