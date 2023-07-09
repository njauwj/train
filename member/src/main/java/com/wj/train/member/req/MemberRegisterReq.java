package com.wj.train.member.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description 用户注册封装类
 */
@Data
public class MemberRegisterReq {

    @NotBlank(message = "手机号不能为空")
    private String mobile;
}
