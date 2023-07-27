package com.wj.train.business.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wj
 * @create_time 2023/7/27
 * @description
 */
@Configuration
public class RabbitmqConfig {


    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("confirmOrder.directExchange", true, false);
    }

    @Bean
    public Queue queue() {
        return new Queue("confirmOrder.queue", true);
    }

    @Bean
    public Binding binding(DirectExchange directExchange, Queue queue) {
        return BindingBuilder.bind(queue).to(directExchange).with("confirmOrder");
    }


}
