package com.fyy.delyoj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.fyy.delyoj.model.dto.question.JudgeCase;
import com.fyy.delyoj.model.dto.question.JudgeConfig;
import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import com.fyy.delyoj.model.entity.Question;
import com.fyy.delyoj.model.enums.JudgeInfoMessageEnum;

import java.util.List;

public class DefaultJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        Long time = judgeInfo.getTime();
        Long memory = judgeInfo.getMemory();
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig bean = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long timeLimit = bean.getTimeLimit();
        Long memoryLimit = bean.getMemoryLimit();
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(time);
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());

        //i:f输出列表和输入列表长度不一致，则判题失败
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        //ii:输出列表和输入列表长度一致，则逐个比较输出列表和输入列表的元素，如果有一个不相等，则判题失败
        for (int i = 0; i < outputList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            if (!judgeCase.getOutput().equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }

        //iii:判断判题条件
        if (memory > memoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        if (time > timeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }


        return judgeInfoResponse;
    }
}
