package com.wj.train.business.controller;

import com.wj.train.business.req.SeatSellReq;
import com.wj.train.business.req.SeatSellResp;
import com.wj.train.business.service.DailyTrainSeatService;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.utils.RespUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author wj
 * @create_time 2023/7/29
 * @description
 */
@RestController
@RequestMapping("/seat-sell")
public class SeatSellController {


    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @GetMapping("/query")
    public CommonResp<List<SeatSellResp>> seatSellCondition(@Valid SeatSellReq seatSellReq) {
        List<SeatSellResp> seatSellRespList = dailyTrainSeatService.seatSellCondition(seatSellReq);
        return RespUtil.success(seatSellRespList);
    }

}
