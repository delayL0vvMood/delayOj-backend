package com.fyy.delyoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fyy.delyoj.annotation.AuthCheck;
import com.fyy.delyoj.common.BaseResponse;
import com.fyy.delyoj.common.DeleteRequest;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.common.ResultUtils;
import com.fyy.delyoj.constant.UserConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.exception.ThrowUtils;
import com.fyy.delyoj.model.dto.exam.ExamQueryRequest;
import com.fyy.delyoj.model.dto.exam.ExamUpdateRequest;
import com.fyy.delyoj.model.dto.examquestion.ExamQuestionAddRequest;
import com.fyy.delyoj.model.dto.examquestion.ExamQuestionQueryRequest;
import com.fyy.delyoj.model.dto.examquestion.ExamQuestionUpdateRequest;
import com.fyy.delyoj.model.entity.Exam;
import com.fyy.delyoj.model.entity.ExamQuestion;
import com.fyy.delyoj.model.entity.Question;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.vo.ExamQuestionVO;
import com.fyy.delyoj.model.vo.ExamVO;
import com.fyy.delyoj.service.ExamQuestionService;
import com.fyy.delyoj.service.ExamService;
import com.fyy.delyoj.service.QuestionService;
import com.fyy.delyoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
@RequestMapping("/exam/question")
@Slf4j
public class ExamQuestionController {

   @Resource
    private ExamQuestionService examQuestionService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private ExamService examService;

    /**
     * 提交题目
     *
     * @param examQuestionAddRequest
     * @param request
     */
    @PostMapping("/add")
    public BaseResponse<Long> addExamQuestion(@RequestBody ExamQuestionAddRequest examQuestionAddRequest,
                                       HttpServletRequest request) {
        if (examQuestionAddRequest == null || examQuestionAddRequest.getQuestionId() <= 0 || examQuestionAddRequest.getExamId() <=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ExamQuestion examQuestion = new ExamQuestion();
        BeanUtils.copyProperties(examQuestionAddRequest, examQuestion);
        examQuestionService.vailExamQuestion(examQuestion, true);
        final User loginUser = userService.getLoginUser(request);
        long examQuestionId = examQuestionService.addExamQuestion(examQuestionAddRequest,loginUser);
        return ResultUtils.success(examQuestionId);
    }


    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteExamQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ExamQuestion oldExamQuestion = examQuestionService.getById(id);
        ThrowUtils.throwIf(oldExamQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        Exam exam = examService.getById(oldExamQuestion.getExamId());
        if (!exam.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = examService.removeById(id);
        return ResultUtils.success(b);
    }
    /**
     * 更新（仅管理员 和出题目者）
     *
     * @param examQuestionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateExam(@RequestBody ExamQuestionUpdateRequest examQuestionUpdateRequest, 
                                            HttpServletRequest request) {
        if (examQuestionUpdateRequest == null || examQuestionUpdateRequest.getExamId() <= 0 || examQuestionUpdateRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(request);
        User user = userService.getById(examService.getById(examQuestionUpdateRequest));
        if(loginUser.getUserRole() != "admin" || user.getId() != loginUser.getId()){
            throw  new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
        }
        
        ExamQuestion examQuestion = new ExamQuestion();
        BeanUtils.copyProperties(examQuestionUpdateRequest, examQuestion);

        // 参数校验
        examQuestionService.vailExamQuestion(examQuestion, false);
        long id = examQuestionUpdateRequest.getId();
        // 判断是否存在
        ExamQuestion oldExamQuestion = examQuestionService.getById(id);
        ThrowUtils.throwIf(oldExamQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = examQuestionService.updateById(examQuestion);
        return ResultUtils.success(result);
    }


    /**
     * 分页获取考试题目列表
     * 功能：根据用户id，题目id，编程语言，题目状态，分页查询
     * 根据考试id查询
     * @param examQuestionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ExamQuestionVO>> listExamQuestionByPage(@RequestBody ExamQuestionQueryRequest examQuestionQueryRequest, HttpServletRequest request) {
        long current = examQuestionQueryRequest.getCurrent();
        long size = examQuestionQueryRequest.getPageSize();


        Page<ExamQuestion> examQuestionPage = examQuestionService.page(new Page<>(current, size),
                examQuestionService.getQueryWrapper(examQuestionQueryRequest));

        final User loginUser = userService.getLoginUser(request);
        /*
        * 脱敏，封装类
        * */
        return ResultUtils.success(examQuestionService
                .getExamQuestionVOPage(examQuestionPage, request));
    }


    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param examQuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ExamQuestionVO>> listMyExamQuestionVOByPage(@RequestBody ExamQuestionQueryRequest examQuestionQueryRequest,
                                                         HttpServletRequest request) {
        if (examQuestionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        examQuestionQueryRequest.setUserId(loginUser.getId());
        long current = examQuestionQueryRequest.getCurrent();
        long size = examQuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ExamQuestion> examQuestionPage = examQuestionService.page(new Page<>(current, size),
                examQuestionService.getQueryWrapper(examQuestionQueryRequest));
        return ResultUtils.success(examQuestionService.getExamQuestionVOPage(examQuestionPage, request));
    }




}
