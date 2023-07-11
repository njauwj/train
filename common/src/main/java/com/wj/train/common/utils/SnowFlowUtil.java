package com.wj.train.common.utils;

import cn.hutool.core.util.IdUtil;

import static com.wj.train.common.constant.SnowflakeConstant.DATACENTER_ID;
import static com.wj.train.common.constant.SnowflakeConstant.WORKER_ID;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description
 */
public class SnowFlowUtil {


    /**
     * 雪花算法生成ID
     *
     * @return
     */
    public static long getSnowFlowId() {
        return IdUtil.getSnowflake(WORKER_ID, DATACENTER_ID).nextId();
    }


}
