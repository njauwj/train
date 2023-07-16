package com.wj.train.business.req;

import com.wj.train.common.req.PageReq;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
@Data
public class DailyTrainStationQueryReq extends PageReq {

    /**
     * 车次编号
     */
    private String trainCode;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
}
