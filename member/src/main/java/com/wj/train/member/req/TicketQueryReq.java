package com.wj.train.member.req;

import com.wj.train.common.req.PageReq;
import lombok.Data;

@Data
public class TicketQueryReq extends PageReq {


    private Long memberId;
}
