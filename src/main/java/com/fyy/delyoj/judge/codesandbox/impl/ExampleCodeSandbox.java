package com.fyy.delyoj.judge.codesandbox.impl;

import com.fyy.delyoj.judge.codesandbox.CodeSandbox;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import com.fyy.delyoj.model.enums.JudgeInfoMessageEnum;
import com.fyy.delyoj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;


/*
* 示例代码沙箱
*
* */
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        String language = executeCodeRequest.getLanguage();
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();



        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMassage("示例代码沙箱判题成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());

        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(100l);
        judgeInfo.setMemory(100l);
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());

        executeCodeResponse.setJudgeInfo(judgeInfo);

        System.out.println("示例代码");
        return executeCodeResponse;
    }
}
