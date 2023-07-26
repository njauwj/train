package com.wj.train.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wj
 * @create_time 2023/7/26
 * @description 用来测试sentinel + feign 的熔断
 */
@RestController
public class HelloController {


    @GetMapping("/hello")
    public String hello() {
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return "hello world";
    }

}
