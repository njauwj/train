package com.wj.train.member.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
@Data
public class MemberLoginReq {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String mobile;


    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

}
