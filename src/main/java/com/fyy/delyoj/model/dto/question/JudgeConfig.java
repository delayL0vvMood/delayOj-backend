package com.fyy.delyoj.model.dto.question;

import lombok.Data;

@Data
public class JudgeConfig {
    /*
    * 时间限制（KB）
    * */
    private Long timeLimit;

    /*
    * 空间限制（KB）
    * */
    private Long memoryLimit;


    /*
    * 堆栈限制（KB）
    * */
    private Long stackLimit;
}
