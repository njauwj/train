package com.wj.train.member.req;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}