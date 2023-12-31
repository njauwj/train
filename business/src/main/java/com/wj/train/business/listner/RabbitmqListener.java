package com.wj.train.business.listner;

import cn.hutool.json.JSONUtil;
import com.wj.train.business.dto.ConfirmOrderMqDto;
import com.wj.train.business.service.ConfirmOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author wj
 * @create_time 2023/7/27
 * @description
 */
@Component
@Slf4j
public class RabbitmqListener {


    @Resource
    private ConfirmOrderService confirmOrderService;

    @RabbitListener(queues = {"confirmOrder.queue"})
    public void confirmOrderListener(Message message) {
        String msg = new String(message.getBody());
        log.info("confirmOrder.queue接收到消息{}", msg);
        ConfirmOrderMqDto confirmOrderMqDto = JSONUtil.toBean(msg, ConfirmOrderMqDto.class);
        try {
            confirmOrderService.handleMqMessage(confirmOrderMqDto);
        } catch (Exception e) {
            log.info("异步处理出现错误{}", e.getMessage());
        }
    }

}
