package com.wj.train.business.listner;

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


    @RabbitListener(queues = {"confirmOrder.queue"})
    public void confirmOrderListener(Message message) {
        log.info("confirmOrder.queue接收到消息{}", new String(message.getBody()));
    }

}
