package com.wj.train.common.intercepter;

import cn.hutool.json.JSONObject;
import com.wj.train.common.context.LocalContext;
import com.wj.train.common.utils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description 用于保存用户信息到本地线程
 */
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        JSONObject jsonObject = JwtTokenUtil.getJSONObject(token);
        LocalContext.set(jsonObject);
        log.info("拦截器拦截请求保存用户信息{}到ThreadLocal", jsonObject);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LocalContext.remove();
    }
}
