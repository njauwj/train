package com.wj.train.business.controller;

import com.wj.train.business.req.ConfirmOrderQueryReq;
import com.wj.train.business.req.ConfirmOrderSaveReq;
import com.wj.train.business.resp.ConfirmOrderQueryResp;
import com.wj.train.business.service.ConfirmOrderService;
import com.wj.train.common.context.LocalContext;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.RespUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/confirm-order")
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
    public CommonResp<Object> confirmOrder(@RequestBody ConfirmOrderSaveReq confirmOrderSaveReq) {
        Long memberId = LocalContext.getMemberId();
        confirmOrderSaveReq.setMemberId(memberId);
        confirmOrderService.confirmOrder(confirmOrderSaveReq);
        return RespUtil.success(true);
    }

}
