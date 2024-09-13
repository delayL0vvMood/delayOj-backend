package com.fyy.delyoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fyy.delyoj.annotation.AuthCheck;
import com.fyy.delyoj.common.BaseResponse;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.common.ResultUtils;
import com.fyy.delyoj.constant.UserConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.model.dto.question.QuestionQueryRequest;
import com.fyy.delyoj.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.fyy.delyoj.model.dto.questionSubmit.QuestionSubmitQueryRequest;
import com.fyy.delyoj.model.entity.Question;
import com.fyy.delyoj.model.entity.QuestionSubmit;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.service.QuestionSubmitService;
import com.fyy.delyoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子点赞接口
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/question_thumb")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public BaseResponse<Long> doSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
            HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long questionId = questionSubmitAddRequest.getQuestionId();
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(questionSubmitId);
    }



    /**
     * 分页获取题目提交列表（仅管理员，用户能查看到除答案的列表）
     * 根据权限过滤答案
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionSubmit>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        Page<QuestionSubmit> questionPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(questionPage);
    }

}
