package com.wj.train.business.feign;

import com.wj.train.business.req.TicketSaveReq;
import com.wj.train.common.resp.CommonResp;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author wj
 * @create_time 2023/7/22
 * @description
 */
@FeignClient(name = "member", url = "http://localhost:8001/member")
@Component
public interface MemberFeign {


    /**
     * 保存会员购票信息
     *
     * @param req
     * @return
     */
    @PostMapping("/admin/ticket/save")
    CommonResp<Object> save(@Valid @RequestBody TicketSaveReq req);
}
