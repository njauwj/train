package com.wj.train.business.req;

import com.wj.train.common.req.PageReq;
import lombok.Data;

@Data
public class TrainStationQueryReq extends PageReq {

    private String trainCode;

}