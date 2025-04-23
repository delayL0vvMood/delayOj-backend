package com.fyy.delyoj.model.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 统一消息体
@Data
public class JudgeMessage {
    private JudgeMessageEnum messageType;
    private Long questionSubmitId;
    private Long examSubmitId;
    private Long userId;
}