package com.fyy.delyoj.judge.strategy;

import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;

/*
* 判题策略
*
* */
public interface JudgeStrategy  {

    JudgeInfo doJudge(JudgeContext judgeContext);

}
