package com.wj.train.member.service;

import com.wj.train.common.resp.CommonResp;
import com.wj.train.member.req.MemberRegisterReq;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
public interface MemberService {


    CommonResp<Long> userRegister(MemberRegisterReq memberRegisterReq);
}
