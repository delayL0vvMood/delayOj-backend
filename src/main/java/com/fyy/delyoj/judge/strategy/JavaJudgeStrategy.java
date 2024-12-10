package com.fyy.delyoj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.fyy.delyoj.model.dto.question.JudgeCase;
import com.fyy.delyoj.model.dto.question.JudgeConfig;
import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import com.fyy.delyoj.model.entity.Question;
import com.fyy.delyoj.model.enums.JudgeInfoMessageEnum;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public class JavaJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        Long time = Optional.ofNullable( judgeInfo.getTime()).orElse(0l);
        Long memory = Optional.ofNullable( judgeInfo.getMemory()).orElse(0l);
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig bean = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long timeLimit = bean.getTimeLimit();
        Long memoryLimit = bean.getMemoryLimit();
        //设置初始判题信息为accepted
        //进行判题，返回判题结果
        //判题样题，跑通流程，没有判题逻辑，直接返回AC
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(time);
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());

        //i:f输出列表和输入列表长度不一致，则判题失败
        if (outputList.size() != inputList.size()) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
            return judgeInfoResponse;
        }

        //ii:输出列表和输入列表长度一致，则逐个比较输出列表和输入列表的元素，如果有一个不相等，则判题失败
        for (int i = 0; i < outputList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            String ans = judgeCase.getOutput();
            String output = outputList.get(i);
            if (!ans.equals(output)) {
                judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
                return judgeInfoResponse;
            }
        }
        //iii:判断判题条件
        if (memory > memoryLimit) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }

        if (time > timeLimit) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }


        return judgeInfoResponse;
    }
}
