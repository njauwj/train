package com.wj.train.member.constant;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
public interface CodeConstant {

    /**
     * 验证码redis存储key
     */
    String CODE_KEY = "train:member:login:code:";

    /**
     * 验证码过期时间
     */
    Long CODE_KEY_TTL = 60L;


}
