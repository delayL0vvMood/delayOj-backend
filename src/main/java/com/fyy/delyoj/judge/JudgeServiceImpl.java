package com.fyy.delyoj.judge;

import cn.hutool.json.JSONUtil;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.judge.codesandbox.CodeSandbox;
import com.fyy.delyoj.judge.codesandbox.CodeSandboxFactory;
import com.fyy.delyoj.judge.codesandbox.CodeSandboxProxy;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.fyy.delyoj.judge.strategy.DefaultJudgeStrategy;
import com.fyy.delyoj.judge.strategy.JudgeContext;
import com.fyy.delyoj.judge.strategy.JudgeStrategy;
import com.fyy.delyoj.model.dto.question.JudgeCase;
import com.fyy.delyoj.model.dto.question.JudgeConfig;
import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import com.fyy.delyoj.model.entity.Question;
import com.fyy.delyoj.model.entity.QuestionSubmit;
import com.fyy.delyoj.model.enums.JudgeInfoMessageEnum;
import com.fyy.delyoj.model.enums.QuestionSubmitLanguageEnum;
import com.fyy.delyoj.model.enums.QuestionSubmitStatusEnum;
import com.fyy.delyoj.service.QuestionService;
import com.fyy.delyoj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    QuestionSubmitService questionSubmitService;

    @Resource
    QuestionService questionService;

    @Value("${codesandbox.type:example}")
    private String type;


    @Override
    public QuestionSubmit doJudge(Long QuestionSubmitId) {
        //1,传入题目提交id，获取题目提交记录，提交信息，题目信息
        QuestionSubmit questionSubmit = questionSubmitService.getById(QuestionSubmitId);
        if(questionSubmit == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"提交记录不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if(question == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在");
        }
        //2，如果不为等待状态，抛出异常
        if(!questionSubmit.getStatus() .equals( QuestionSubmitStatusEnum.WAITING.getValue())){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"正在判题中，请勿重复提交");
        }

        //3，如果题目是等待状态更新状态为运行中
        QuestionSubmit updateQuestionSubmit = new QuestionSubmit();
        updateQuestionSubmit.setId(QuestionSubmitId);
        updateQuestionSubmit.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(updateQuestionSubmit);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }

        //4，调用代码沙箱，获取判题结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        //lamda表达式 获取输入列表,输入用例
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());

        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        /*
         * 链式调用
         * */
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();

        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        //5，根据沙箱执行结果，设置判题状态和提交信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(executeCodeResponse.getOutputList());
        judgeContext.setQuestion(question);
        judgeContext.setJudgeCaseList(judgeCaseList);
            //策略模式选择
        JudgeManager judgeManager = new JudgeManager();
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        //修改数据库中的判题结果
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(QuestionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");

        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(QuestionSubmitId);
        return questionSubmitResult;

    }
}
