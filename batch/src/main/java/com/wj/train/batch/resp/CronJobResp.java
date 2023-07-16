package com.wj.train.batch.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class CronJobResp {
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
     * 任务状态
     */
    private String state;

    /**
     *  cron 表达式
     */
    private String cronExpression;

    /**
     * 下次任务执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nextFireTime;

    /**
     * 上次任务执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date preFireTime;

}
