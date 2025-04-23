package com.fyy.delyoj.rabbitmq;

import com.fyy.delyoj.model.dto.message.JudgeMessage;
import com.fyy.delyoj.model.dto.message.JudgeMessageEnum;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, JudgeMessage message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
