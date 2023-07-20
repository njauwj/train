package com.wj.train.business.controller;

import com.wj.train.business.resp.StationQueryResp;
import com.wj.train.business.service.StationService;
import com.wj.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/station")
public class StationController {

    @Resource
    private StationService stationService;


    @GetMapping("/query-all")
    public CommonResp<List<StationQueryResp>> queryAll() {
        List<StationQueryResp> stationQueryResp = stationService.queryAll();
        return new CommonResp<>(stationQueryResp);
    }
}
