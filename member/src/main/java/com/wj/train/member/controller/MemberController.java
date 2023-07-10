package com.wj.train.member.controller;

import com.wj.train.common.resp.CommonResp;
import com.wj.train.member.req.MemberLoginReq;
import com.wj.train.member.req.MemberSendCodeReq;
import com.wj.train.member.resp.MemberLoginResp;
import com.wj.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;


    @PostMapping("/send-code")
    public CommonResp<Boolean> sendCode(@Valid @RequestBody MemberSendCodeReq memberSendCodeReq) {
        return memberService.sendCode(memberSendCodeReq);
    }

    @PostMapping("/login")
    public CommonResp<MemberLoginResp> login(@Valid @RequestBody MemberLoginReq memberLoginReq) {
        return memberService.login(memberLoginReq);
    }
}
