package com.wj.train.batch.req;

import lombok.Data;

@Data
public class CronJobReq {
    private String group;

    private String name;

    private String description;

    private String cronExpression;

}
