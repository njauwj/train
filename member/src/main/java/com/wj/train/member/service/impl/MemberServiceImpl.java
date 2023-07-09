package com.wj.train.member.service.impl;

import cn.hutool.core.util.IdUtil;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.exception.BusinessExceptionEnum;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.utils.RespUtil;
import com.wj.train.member.domain.Member;
import com.wj.train.member.domain.MemberExample;
import com.wj.train.member.mapper.MemberMapper;
import com.wj.train.member.req.MemberRegisterReq;
import com.wj.train.member.service.MemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wj.train.common.constant.SnowflakeConstant.DATACENTER_ID;
import static com.wj.train.common.constant.SnowflakeConstant.WORKER_ID;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
@Service
public class MemberServiceImpl implements MemberService {

    @Resource
    private MemberMapper memberMapper;

    /**
     * 用户注册
     *
     * @param memberRegisterReq
     * @return
     */
    @Override
    public CommonResp<Long> userRegister(MemberRegisterReq memberRegisterReq) {
        String mobile = memberRegisterReq.getMobile();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);
        if (!members.isEmpty()) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }
        Member member = new Member();
        member.setMobile(mobile);
        member.setId(IdUtil.getSnowflake(WORKER_ID, DATACENTER_ID).nextId());
        memberMapper.insert(member);
        return RespUtil.success(member.getId());
    }
}
