package com.fyy.delyoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fyy.delyoj.common.BaseResponse;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.common.ResultUtils;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.model.dto.examsubmit.ExamSubmitAddRequest;
import com.fyy.delyoj.model.dto.examsubmit.ExamSubmitQueryRequest;
import com.fyy.delyoj.model.entity.ExamSubmit;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.vo.ExamSubmitVO;
import com.fyy.delyoj.service.ExamSubmitService;
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
@RequestMapping("/exam/submit")
@Slf4j
public class ExamSubmitController {

    @Resource
    private ExamSubmitService examSubmitService;

    @Resource
    private UserService userService;

/*    *
     * 提交题目
     *
     * @param examSubmitAddRequest
     * @param request
     */
    @PostMapping("/")
    public BaseResponse<Long> doSubmit(@RequestBody ExamSubmitAddRequest examSubmitAddRequest,
                                       HttpServletRequest request) {
        if (examSubmitAddRequest == null || examSubmitAddRequest.getQuestionId() <= 0 || examSubmitAddRequest.getExamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long questionId = examSubmitAddRequest.getQuestionId();
        long examSubmitId = examSubmitService.doExamSubmit(examSubmitAddRequest, loginUser);
        return ResultUtils.success(examSubmitId);
    }



/*    *
     * 分页获取题目提交列表（仅管理员，用户能查看到除答案的列表）
     * 根据权限过滤答案
     * 功能：根据用户id，题目id，编程语言，题目状态，分页查询
     * @param examSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ExamSubmitVO>> listExamSubmitByPage(@RequestBody ExamSubmitQueryRequest examSubmitQueryRequest, HttpServletRequest request) {
        long current = examSubmitQueryRequest.getCurrent();
        long size = examSubmitQueryRequest.getPageSize();

        
/*        * 先查出所有题目提交，然后根据用户id，题目id，编程语言，题目状态，分页查询
        * 从数据库中查到原始分页信息
        * */
        Page<ExamSubmit> examSubmitPage = examSubmitService.page(new Page<>(current, size),
                examSubmitService.getQueryWrapper(examSubmitQueryRequest));

        final User loginUser = userService.getLoginUser(request);
        /*
         * 脱敏，封装类
         * */
        return ResultUtils.success(examSubmitService
                .getExamSubmitVOPage(examSubmitPage, loginUser));
    }

}
