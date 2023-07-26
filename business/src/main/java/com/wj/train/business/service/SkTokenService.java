package com.wj.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wj.train.business.domain.SkToken;
import com.wj.train.business.domain.SkTokenExample;
import com.wj.train.business.mapper.SkTokenMapper;
import com.wj.train.business.req.SkTokenQueryReq;
import com.wj.train.business.req.SkTokenSaveReq;
import com.wj.train.business.resp.SkTokenQueryResp;
import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.PageResp;
import com.wj.train.common.utils.SnowFlowUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.wj.train.common.exception.BusinessExceptionEnum.*;

@Service
@Slf4j
public class SkTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(SkTokenService.class);

    @Resource
    private SkTokenMapper skTokenMapper;

    @Resource
    private TrainStationService trainStationService;

    @Resource
    private TrainCarriageService trainCarriageService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    public void save(SkTokenSaveReq req) {
        String trainCode = req.getTrainCode();
        Date date = req.getDate();
        Integer tokenCount = req.getCount();
        DateTime now = DateTime.now();
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if (ObjectUtil.isNull(skToken.getId())) {
            skToken.setId(SnowFlowUtil.getSnowFlowId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        } else {
            skToken.setUpdateTime(now);
            skTokenMapper.updateByPrimaryKey(skToken);
            //将令牌数量放入缓存中
            String tokenKey = "skToken:" + trainCode + ":" + date;
            stringRedisTemplate.opsForValue().set(tokenKey, tokenCount.toString(), 60, TimeUnit.SECONDS);
        }
    }

    public PageResp<SkTokenQueryResp> queryList(SkTokenQueryReq req) {
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.setOrderByClause("id desc");
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<SkToken> skTokenList = skTokenMapper.selectByExample(skTokenExample);

        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokenList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<SkTokenQueryResp> list = BeanUtil.copyToList(skTokenList, SkTokenQueryResp.class);

        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        skTokenMapper.deleteByPrimaryKey(id);
    }

    /**
     * 生成每日车次的令牌
     *
     * @param trainCode
     * @param date
     */
    public void genSkToken(String trainCode, Date date) {
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        long count = skTokenMapper.countByExample(skTokenExample);
        if (count > 0) {
            //删除原有的车次令牌
            skTokenMapper.deleteByExample(skTokenExample);
        }
        SkToken skToken = new SkToken();
        skToken.setId(SnowFlowUtil.getSnowFlowId());
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setCreateTime(DateTime.now());
        skToken.setUpdateTime(DateTime.now());
        int trainStationsNums = trainStationService.getStationsByTrainCode(trainCode).size();
        Integer totalSeatsNums = trainCarriageService.getTotalSeatsNums(trainCode);
        Integer tokenCount = (trainStationsNums - 1) * totalSeatsNums;
        skToken.setCount(tokenCount);
        skTokenMapper.insert(skToken);
        //将令牌数量放入缓存中
        String tokenKey = "skToken:" + trainCode + ":" + date;
        stringRedisTemplate.opsForValue().set(tokenKey, tokenCount.toString(), 60, TimeUnit.SECONDS);
    }

    /**
     * 购票前先拿令牌，防止同一个人用机器刷票
     *
     * @param trainCode
     * @param date
     * @param memberId
     * @return
     */
    public void takeSkTone(String trainCode, Date date, Long memberId) {
        String redisKey = "skToken:" + trainCode + date + memberId;
        String tokenKey = "skToken:" + trainCode + ":" + date;
        RLock lock = redissonClient.getLock(redisKey);
        try {
            //注意不需要主动释放锁，采用锁过期释放，同一个用户五秒内同车次只能请求一次
            boolean tryLock = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (!tryLock) {
                log.info("{}获取令牌过于频繁，稍后重试", memberId);
                throw new BusinessException(BUSINESS_CONFIRM_ORDER_BUSY);
            }
            SkTokenExample skTokenExample = new SkTokenExample();
            skTokenExample.createCriteria().andTrainCodeEqualTo(trainCode).andDateEqualTo(date);
            //扣减令牌
            String skTokensStr = stringRedisTemplate.opsForValue().get(tokenKey);
            Integer skTokens = 0;
            if (CharSequenceUtil.isBlank(skTokensStr)) {
                SkToken skToken = skTokenMapper.selectByExample(skTokenExample).get(0);
                skTokens = skToken.getCount();
            } else {
                skTokens = Integer.parseInt(skTokensStr);
            }
            if (skTokens < 0) {
                throw new BusinessException(BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR);
            }
            skTokens -= 1;
            stringRedisTemplate.opsForValue().set(tokenKey, skTokens.toString(), 60, TimeUnit.SECONDS);
            if (skTokens % 5 == 0) {
                //先扣减缓存，再根据缓存数量定量更新数据库，减缓数据库压力
                SkToken skToken = new SkToken();
                skToken.setCount(skTokens);
                skTokenMapper.updateByExampleSelective(skToken, skTokenExample);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
