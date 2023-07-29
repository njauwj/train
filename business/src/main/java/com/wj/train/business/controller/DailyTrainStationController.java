package com.wj.train.business.controller;

import com.wj.train.business.req.DailyTrainStationQueryReq;
import com.wj.train.business.resp.DailyTrainStationQueryResp;
import com.wj.train.business.service.DailyTrainStationService;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.utils.RespUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/daily-train-station")
public class DailyTrainStationController {

    @Resource
    private DailyTrainStationService dailyTrainStationService;

    @GetMapping("/query-by-train-code")
    public CommonResp<List<DailyTrainStationQueryResp>> queryByTrainCode(DailyTrainStationQueryReq dailyTrainStationQueryReq) {
        List<DailyTrainStationQueryResp> dailyTrainStationQueryRespList = dailyTrainStationService.queryByTrainCode(dailyTrainStationQueryReq);
        return RespUtil.success(dailyTrainStationQueryRespList);
    }
}
