package com.wj.train.business.req;

import com.wj.train.common.req.PageReq;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DailyTrainQueryReq extends PageReq {

    /**
     * 车次
     */
    private String code;

    /**
     * 发车日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

}
