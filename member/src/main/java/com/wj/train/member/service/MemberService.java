package com.wj.train.member.service;

import com.wj.train.common.resp.CommonResp;
import com.wj.train.member.req.MemberLoginReq;
import com.wj.train.member.req.MemberSendCodeReq;
import com.wj.train.member.resp.MemberLoginResp;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
public interface MemberService {


    CommonResp<Boolean> sendCode(MemberSendCodeReq memberSendCodeReq);

    CommonResp<MemberLoginResp> login(MemberLoginReq memberLoginReq);

}
