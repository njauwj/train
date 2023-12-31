package com.wj.train.gateway.utils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.crypto.GlobalBouncyCastleProvider;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Slf4j
public class JwtTokenUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenUtil.class);

    /**
     * key – HS256(HmacSHA256)密钥，不能泄漏，且每个项目都应该不一样，可以放到配置文件中,用来加解密
     */
    private static final String KEY = "wj12306";

    /**
     * 生成token
     *
     * @param payload
     * @return
     */
    public static String createToken(Map<String, Object> payload) {
        GlobalBouncyCastleProvider.setUseBouncyCastle(false);
        DateTime now = DateTime.now();
        DateTime expTime = now.offsetNew(DateField.HOUR, 24);
        // 签发时间
        payload.put(RegisteredPayload.ISSUED_AT, now);
        // 过期时间
        payload.put(RegisteredPayload.EXPIRES_AT, expTime);
        // 生效时间
        payload.put(RegisteredPayload.NOT_BEFORE, now);
        String token = JWTUtil.createToken(payload, KEY.getBytes());
        LOG.info("生成JWT token：{}", token);
        return token;
    }

    /**
     * 检验token是否有效
     *
     * @param token
     * @return
     */
    public static boolean validate(String token) {
        GlobalBouncyCastleProvider.setUseBouncyCastle(false);
        try {
            JWT jwt = JWTUtil.parseToken(token).setKey(KEY.getBytes());
            // validate包含了verify
            boolean validate = jwt.validate(0);
            LOG.info("JWT token校验结果：{}", validate);
            return validate;
        } catch (Exception e) {
            log.info("检验token异常{}", e.getMessage());
            return false;
        }
    }

    public static JSONObject getJSONObject(String token) {
        GlobalBouncyCastleProvider.setUseBouncyCastle(false);
        JWT jwt = JWTUtil.parseToken(token).setKey(KEY.getBytes());
        JSONObject payloads = jwt.getPayloads();
        payloads.remove(RegisteredPayload.ISSUED_AT);
        payloads.remove(RegisteredPayload.EXPIRES_AT);
        payloads.remove(RegisteredPayload.NOT_BEFORE);
        LOG.info("根据token获取原始内容：{}", payloads);
        return payloads;
    }

}
