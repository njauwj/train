package com.wj.train.member.config;

import com.wj.train.common.intercepter.TokenInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description
 */
@Component
public class InterceptorConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor())
                .order(0)
                .addPathPatterns("/**")
                .excludePathPatterns("/member/member/send-code", "/member/member/login");
    }
}
