package com.fyy.delyoj.judge;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.judge.codesandbox.CodeSandbox;
import com.fyy.delyoj.judge.codesandbox.CodeSandboxFactory;
import com.fyy.delyoj.judge.codesandbox.CodeSandboxProxy;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.fyy.delyoj.judge.strategy.JudgeContext;
import com.fyy.delyoj.model.dto.question.JudgeCase;
import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import com.fyy.delyoj.model.dto.userscore.UserScoreAddRequest;
import com.fyy.delyoj.model.entity.ExamSubmit;
import com.fyy.delyoj.model.entity.Question;
import com.fyy.delyoj.model.entity.QuestionSubmit;
import com.fyy.delyoj.model.entity.UserScore;
import com.fyy.delyoj.model.enums.JudgeInfoMessageEnum;
import com.fyy.delyoj.model.enums.QuestionSubmitStatusEnum;
import com.fyy.delyoj.service.ExamSubmitService;
import com.fyy.delyoj.service.QuestionService;
import com.fyy.delyoj.service.QuestionSubmitService;
import com.fyy.delyoj.service.UserScoreService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    QuestionSubmitService questionSubmitService;

    @Resource
    QuestionService questionService;

    @Resource
    ExamSubmitService examSubmitService;

    @Resource
    UserScoreService userScoreService;

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
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(QuestionSubmitId);
        if(executeCodeResponse.getStatus() == 1) {
            JudgeContext judgeContext = new JudgeContext();
            judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
            judgeContext.setInputList(inputList);
            judgeContext.setOutputList(executeCodeResponse.getOutputList());
            judgeContext.setQuestion(question);
            judgeContext.setJudgeCaseList(judgeCaseList);
            judgeContext.setQuestionSubmit(questionSubmit);
            //策略模式选择
            JudgeManager judgeManager = new JudgeManager();
            JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
            if(JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())){
                LambdaUpdateWrapper<Question> questionLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                questionLambdaUpdateWrapper.eq(Question::getId,questionId)
                        .setSql("acceptedNum = acceptedNum + 1");
                questionService.update(null,questionLambdaUpdateWrapper);
            }
            //修改数据库中的判题结果
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        }else{
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getValue());
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        }
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(QuestionSubmitId);
        return questionSubmitResult;

    }


    @Override
    public ExamSubmit doExamJudge(Long questionSubmitId, Long examSubmitId, Long userId) {
        doJudge(questionSubmitId);
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        ExamSubmit examSubmitUpdate = new ExamSubmit();
        String judgeInfo = questionSubmit.getJudgeInfo();
        Integer status = questionSubmit.getStatus();

        examSubmitUpdate.setId(examSubmitId);
        examSubmitUpdate.setJudgeInfo(judgeInfo);
        examSubmitUpdate.setStatus(status);

        ExamSubmit examSubmit = examSubmitService.getById(examSubmitId);
        Long examId = examSubmit.getExamId();
        Long questionId = examSubmit.getQuestionId();
        JudgeInfo bean = JSONUtil.toBean(judgeInfo, JudgeInfo.class);
        UserScoreAddRequest userScoreAddRequest = new UserScoreAddRequest();
        userScoreAddRequest.setExamId(examId);
        userScoreAddRequest.setUserId(userId);
        userScoreAddRequest.setQuestionId(questionId);
        userScoreAddRequest.setJudgeResult(bean.getMessage());
        if (StringUtils.equals(bean.getMessage(), JudgeInfoMessageEnum.ACCEPTED.getValue())) {
            userScoreAddRequest.setScore(1);
        } else {
            userScoreAddRequest.setScore(0);
        }
        userScoreService.doUserScore(userScoreAddRequest,userId);

        boolean update = examSubmitService.updateById(examSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        ExamSubmit examSubmitResult = examSubmitService.getById(examSubmitId);
        System.out.println(questionSubmit);
        System.out.println(examSubmitResult);
        return examSubmitResult;
    }

}
