package com.fyy.delyoj.rabbitmq;

import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.judge.JudgeService;
import com.fyy.delyoj.model.dto.message.JudgeMessage;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class MessageCustomer {

    @Resource
    JudgeService judgeService;

    // 指定程序监听的消息队列和确认机制
    @SneakyThrows

    @RabbitListener(
            queues = {"code_queue"},
            ackMode = "MANUAL",
            messageConverter = "jsonMessageConverter" // 使用JSON转换
    )
    public void receiveMessage(JudgeMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        try {
            switch (message.getMessageType()) {
                case QUESTION_SUBMIT:
                    judgeService.doJudge(message.getQuestionSubmitId());
                    break;
                case EXAM_SUBMIT:
                    judgeService.doExamJudge(
                            message.getQuestionSubmitId(),
                            message.getExamSubmitId(),
                            message.getUserId()
                    );
                    break;
                default:
                    log.error("未知消息类型: {}", message.getMessageType());
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("消息处理失败", e);
            channel.basicNack(deliveryTag, false, false); // 不重试
        }
    }

}
