package com.wj.train.common.utils;

import com.wj.train.common.resp.CommonResp;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description 统一返回工具类
 */
public class RespUtil {


    public static <T> CommonResp<T> success(T content, String message) {
        return new CommonResp<>(content, message);
    }

    public static <T> CommonResp<T> success(T content) {
        return new CommonResp<>(content);
    }

    public static CommonResp<Object> error(String message) {
        return new CommonResp<>(false, message);
    }


}
