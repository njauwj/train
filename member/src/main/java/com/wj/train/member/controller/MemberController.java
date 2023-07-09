package com.wj.train.member.controller;

import com.wj.train.common.resp.CommonResp;
import com.wj.train.member.req.MemberRegisterReq;
import com.wj.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
@RestController
public class MemberController {

    @Resource
    private MemberService memberService;


    @PostMapping("/register")
    public CommonResp<Long> register(@Valid MemberRegisterReq memberRegisterReq) {
        return memberService.userRegister(memberRegisterReq);
    }

}
