package com.wj.train.business.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.wj.train.business.req.ConfirmOrderQueryReq;
import com.wj.train.business.req.ConfirmOrderSaveReq;
import com.wj.train.business.resp.ConfirmOrderQueryResp;
import com.wj.train.business.service.ConfirmOrderService;
import com.wj.train.common.context.LocalContext;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.RespUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.wj.train.common.exception.BusinessExceptionEnum.BUSINESS_TOO_MANY_PEOPLE;

@RestController
@RequestMapping("/confirm-order")
@Slf4j
public class ConfirmOrderController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<ConfirmOrderQueryResp>> queryList(@Valid ConfirmOrderQueryReq req) {
        PageResp<ConfirmOrderQueryResp> list = confirmOrderService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        confirmOrderService.delete(id);
        return new CommonResp<>();
    }

    @PostMapping("/do")
    @SentinelResource(value = "confirmOrderController",blockHandler = "confirmOrderBlockHandler")
    public CommonResp<Object> confirmOrder(@RequestBody ConfirmOrderSaveReq confirmOrderSaveReq) {
        Long memberId = LocalContext.getMemberId();
        confirmOrderSaveReq.setMemberId(memberId);
        confirmOrderService.confirmOrder(confirmOrderSaveReq);
        return RespUtil.success(true);
    }

    /**
     * 限流降级方法
     */
    public CommonResp<Object> confirmOrderBlockHandler(ConfirmOrderSaveReq confirmOrderSaveReq, BlockException blockException) {
        log.info("接口/confirm-order/do 触发限流降级");
        throw new BusinessException(BUSINESS_TOO_MANY_PEOPLE);
    }

}
