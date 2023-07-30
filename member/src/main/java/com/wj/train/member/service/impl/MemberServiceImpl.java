package com.wj.train.member.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.utils.JwtTokenUtil;
import com.wj.train.common.utils.RespUtil;
import com.wj.train.common.utils.SnowFlowUtil;
import com.wj.train.member.domain.Member;
import com.wj.train.member.domain.MemberExample;
import com.wj.train.member.mapper.MemberMapper;
import com.wj.train.member.req.MemberLoginReq;
import com.wj.train.member.req.MemberSendCodeReq;
import com.wj.train.member.resp.MemberLoginResp;
import com.wj.train.member.service.MemberService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.wj.train.common.exception.BusinessExceptionEnum.*;
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

    @Resource
    private RedissonClient redissonClient;

    /**
     * 用户注册
     *
     * @param
     * @return
     */
    public void userRegister(String mobile) {
        Member member = new Member();
        member.setMobile(mobile);
        member.setId(SnowFlowUtil.getSnowFlowId());
        memberMapper.insert(member);
    }

    /**
     * 查询数据库该手机号有没有注册
     *
     * @param mobile
     */
    private Member getMemberByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);
        return members.isEmpty() ? null : members.get(0);
    }

    /**
     * 验证码发送
     *
     * @param memberSendCodeReq
     * @return
     */
    @Override
    public CommonResp<Boolean> sendCode(MemberSendCodeReq memberSendCodeReq) {
        protectMessageService(memberSendCodeReq);
        String mobile = memberSendCodeReq.getMobile();
        //1. 查询该手机号是否注册
        Member member = getMemberByMobile(mobile);
        if (member == null) {
            //不存在则自动注册
            log.info("手机号{}不存在自动注册", mobile);
            userRegister(mobile);
        }
        //2. 生成验证码
        String code = RandomUtil.randomNumbers(4);
        //3. TODO 发送验证码 可以对接第三方服务
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //TODO 先写死 8888 方便测试
        ops.set(CODE_KEY + mobile, "8888", CODE_KEY_TTL, TimeUnit.SECONDS);
        log.info("验证码{}", code);
        return RespUtil.success(true);
    }

    /**
     * 防止短信被刷,使用令牌桶限流算法
     */
    private void protectMessageService(MemberSendCodeReq memberSendCodeReq) {
        String mobile = memberSendCodeReq.getMobile();
        String rateKey = "member:sendCode:rate" + mobile;
        String lockKey = "member:sendCode:lock" + mobile;
        String s = stringRedisTemplate.opsForValue().get(lockKey);
        if (!CharSequenceUtil.isBlank(s)) {
            log.info("{}请求过于频繁，被拉黑15分钟", mobile);
            throw new BusinessException(MEMBER_SEND_CODE_TOO_MANY);
        }
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateKey);
        rateLimiter.expire(Instant.now().plusSeconds(3600));
        //每60秒产生5个令牌
        rateLimiter.trySetRate(RateType.OVERALL, 5, 60, RateIntervalUnit.SECONDS);
        //尝试获取一个令牌
        boolean acquire = rateLimiter.tryAcquire(1);
        if (!acquire) {
            //如果一个号码1分钟内请求超过5次就封禁15分钟
            stringRedisTemplate.opsForValue().set(lockKey, mobile, 15, TimeUnit.MINUTES);
            throw new BusinessException(MEMBER_SEND_CODE_TOO_MANY);
        }
    }

    /**
     * 登入
     *
     * @param memberLoginReq
     * @return
     */
    @Override
    public CommonResp<MemberLoginResp> login(MemberLoginReq memberLoginReq) {
        String mobile = memberLoginReq.getMobile();
        Member member = getMemberByMobile(mobile);
        if (member == null) {
            throw new BusinessException(MEMBER_MOBILE_NOT_EXIST);
        }
        String code = memberLoginReq.getCode();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String actualCode = ops.get(CODE_KEY + mobile);
        if (!code.equals(actualCode)) {
            //验证码错误
            throw new BusinessException(MEMBER_MOBILE_CODE_ERROR);
        }
        MemberLoginResp memberLoginResp = new MemberLoginResp();
        BeanUtils.copyProperties(member, memberLoginResp);
        //将用户信息封装成map用于生成JWT token
        Map<String, Object> payload = BeanUtil.beanToMap(memberLoginResp);
        String token = JwtTokenUtil.createToken(payload);
        memberLoginResp.setToken(token);
        return RespUtil.success(memberLoginResp);
    }
}
