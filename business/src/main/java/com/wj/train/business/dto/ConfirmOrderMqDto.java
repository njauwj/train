package com.wj.train.business.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author wj
 * @create_time 2023/7/27
 * @description
 */
@Data
public class ConfirmOrderMqDto {


    private String trainCode;

    private Date date;

}
