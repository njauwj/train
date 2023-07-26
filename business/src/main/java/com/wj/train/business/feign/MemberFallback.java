package com.wj.train.business.feign;

import com.wj.train.business.req.TicketSaveReq;
import com.wj.train.common.resp.CommonResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author wj
 * @create_time 2023/7/26
 * @description member 服务远程调用的降级处理
 */
@Component
@Slf4j
public class MemberFallback implements MemberFeign{
    @Override
    public CommonResp<Object> save(TicketSaveReq req) {
        return null;
    }

    @Override
    public String hello()  {
        log.info("member服务的hello接口降级处理");
        return "member-hello-fallback";
    }
}
