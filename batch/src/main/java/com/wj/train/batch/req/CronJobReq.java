package com.wj.train.batch.req;

import lombok.Data;

@Data
public class CronJobReq {
    /**
     * 任务分组
     */
    private String group;
    /**
     * 任务所在类的全路径 如：com.wj.train.batch.job.DailyTrainJob
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * cron 表达式
     */
    private String cronExpression;

}
