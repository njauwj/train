package com.wj.train.member.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class PassengerAddReq {

    private Long id;

    private Long memberId;

    @NotBlank(message = "乘客姓名不能为空")
    private String name;
    @NotBlank(message = "乘客身份证不能为空")
    private String idCard;
    @NotBlank(message = "乘客类型不能为空")
    private String type;

    private Date createTime;

    private Date updateTime;

}