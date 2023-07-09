package com.wj.train.member.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.utils.RespUtil;
import com.wj.train.member.domain.Member;
import com.wj.train.member.domain.MemberExample;
import com.wj.train.member.mapper.MemberMapper;
import com.wj.train.member.req.MemberLoginReq;
import com.wj.train.member.req.MemberSendCodeReq;
import com.wj.train.member.service.MemberService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.wj.train.common.constant.SnowflakeConstant.DATACENTER_ID;
import static com.wj.train.common.constant.SnowflakeConstant.WORKER_ID;
import static com.wj.train.common.exception.BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR;
import static com.wj.train.member.constant.CodeConstant.CODE_KEY;
import static com.wj.train.member.constant.CodeConstant.CODE_KEY_TTL;

/**
 * @author wj
 * @create_time 2023/7/9
 * @description
 */
@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     *
     * @param
     * @return
     */
    public void userRegister(String mobile) {
        Member member = new Member();
        member.setMobile(mobile);
        member.setId(IdUtil.getSnowflake(WORKER_ID, DATACENTER_ID).nextId());
        memberMapper.insert(member);
    }

    /**
     * 查询数据库该手机号有没有注册
     *
     * @param mobile
     */
    private boolean isMobileExist(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);
        return members.isEmpty();
    }

    /**
     * 验证码发送
     *
     * @param memberSendCodeReq
     * @return
     */
    @Override
    public CommonResp<Boolean> sendCode(MemberSendCodeReq memberSendCodeReq) {
        String mobile = memberSendCodeReq.getMobile();
        //1. 查询该手机号是否注册
        boolean mobileExist = isMobileExist(mobile);
        if (mobileExist) {
            //不存在则自动注册
            userRegister(mobile);
        }
        //2. 生成验证码
        String code = RandomUtil.randomNumbers(4);
        //3. TODO 发送验证码 可以对接第三方服务
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set(CODE_KEY + mobile, code, CODE_KEY_TTL, TimeUnit.SECONDS);
        log.info("验证码{}", code);
        return RespUtil.success(true);
    }

    /**
     * 登入
     *
     * @param memberLoginReq
     * @return
     */
    @Override
    public CommonResp<Object> login(MemberLoginReq memberLoginReq) {
        String mobile = memberLoginReq.getMobile();
        String code = memberLoginReq.getCode();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String actualCode = ops.get(CODE_KEY + mobile);
        if (!code.equals(actualCode)) {
            //验证码错误
            throw new BusinessException(MEMBER_MOBILE_CODE_ERROR);
        }
        return RespUtil.success(null);
    }
}
