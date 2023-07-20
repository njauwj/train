package com.wj.train.business.domain;

import lombok.Data;

/**
 * @author wj
 * @create_time 2023/7/19
 * @description
 */
@Data
public class Ticket {

    private Long passengerId;
    private String passengerIdCard;
    private String passengerName;
    private String passengerType;
    private String seat;
    private String seatTypeCode;

}
