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
import com.fyy.delyoj.model.vo.QuestionSubmitVO;
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
@RequestMapping("/question_submit")
@Slf4j
@Deprecated
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
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
     * 功能：根据用户id，题目id，编程语言，题目状态，分页查询
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();

        /*
        * 先查出所有题目提交，然后根据用户id，题目id，编程语言，题目状态，分页查询
        * 从数据库中查到原始分页信息
        * */
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));

        final User loginUser = userService.getLoginUser(request);
        /*
        * 脱敏，封装类
        * */
        return ResultUtils.success(questionSubmitService
                .getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

}
