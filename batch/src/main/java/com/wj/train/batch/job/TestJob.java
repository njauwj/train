package com.wj.train.batch.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author wj
 * @create_time 2023/7/16
 * @description
 */
@DisallowConcurrentExecution
public class TestJob implements Job {


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("hello world");
    }
}
