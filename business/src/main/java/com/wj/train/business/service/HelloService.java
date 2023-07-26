package com.wj.train.business.service;

import com.wj.train.business.feign.MemberFeign;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author wj
 * @create_time 2023/7/26
 * @description
 */
@Service
public class HelloService {
    @Resource
    private MemberFeign memberFeign;

    public String hello() {
        return memberFeign.hello();
    }
}
