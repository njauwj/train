package com.wj.train.business.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.wj.train.business.service.HelloService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wj
 * @create_time 2023/7/26
 * @description
 */
@RestController
public class HelloController {



    @Resource
    private HelloService helloService;

    @GetMapping("/hello")
    @SentinelResource(value = "helloController")
    public String hello() {
        return helloService.hello();
    }

}
