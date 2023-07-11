package com.wj.train.gateway.filter;

import com.wj.train.gateway.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 全局过滤器
 */
@Configuration
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 无需拦截的请求
     */
    private static final List<String> WHITE_REQUEST = Arrays.asList(
            "/member/member/send-code",
            "/member/member/login"
    );


    @Bean
    public GlobalFilter customFilter() {
        return new CustomGlobalFilter();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        if (WHITE_REQUEST.contains(path)) {
            log.info("不需要拦截的请求{}", path);
            return chain.filter(exchange);
        }
        HttpHeaders headers = request.getHeaders();
        List<String> tokens = headers.get("token");
        if (tokens == null || tokens.isEmpty()) {
            log.info("token不存在");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        String token = tokens.get(0);
        //检验token是否有效
        boolean validate = JwtTokenUtil.validate(token);
        if (!validate) {
            log.info("token无效");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        return chain.filter(exchange);
    }


    /**
     * 多个过滤器的执行顺序，越小越先执行
     *
     * @return
     */
    @Override
    public int getOrder() {
        return -1;
    }
}