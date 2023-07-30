package com.wj.train.member.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.tea.TeaException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author wj
 * @create_time 2023/7/30
 * @description
 */
@Component
@Slf4j
public class AliyuncsUtil {

    @Resource
    private Client aliyunClient;

    /**
     * 发送短信
     */
    public void sendMessage(String phoneNumber, String code) {
        String template = "{\"code\":\"" + code + "\"}";
        com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setPhoneNumbers(phoneNumber)
                .setTemplateParam(template);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            aliyunClient.sendSmsWithOptions(sendSmsRequest, runtime);
            log.info("短信验证码发送成功{}", code);
        } catch (TeaException error) {
            // 如有需要，请打印 error
            log.info("发送短信出现错误{}", error.getMessage());
        } catch (Exception e) {
            log.info("发送短信出现错误{}", e.getMessage());
        }
    }

}
