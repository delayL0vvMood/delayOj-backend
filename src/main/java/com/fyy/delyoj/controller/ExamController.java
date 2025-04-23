package com.fyy.delyoj.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fyy.delyoj.annotation.AuthCheck;
import com.fyy.delyoj.common.BaseResponse;
import com.fyy.delyoj.common.DeleteRequest;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.common.ResultUtils;
import com.fyy.delyoj.constant.UserConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.exception.ThrowUtils;
import com.fyy.delyoj.model.dto.exam.*;
import com.fyy.delyoj.model.entity.Exam;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.vo.ExamVO;
import com.fyy.delyoj.service.ExamService;
import com.fyy.delyoj.service.ExamSubmitService;
import com.fyy.delyoj.service.UserService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
/**
 * 问题接口
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/exam")
@Slf4j
public class ExamController {

    @Resource
    private ExamService examService;

    @Resource
    private UserService userService;
    private  final static Gson GSON = new Gson();

    @Resource
    private ExamSubmitService examSubmitService;

    // region 增删改查

    /**
     * 创建
     * @param examAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addExam(@RequestBody ExamAddRequest examAddRequest,
                                      HttpServletRequest request) {
        if (examAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Exam exam = new Exam();
        BeanUtils.copyProperties(examAddRequest, exam);


        examService.validExam(exam, true);
        User loginUser = userService.getLoginUser(request);
        exam.setUserId(loginUser.getId());
        boolean result = examService.saveExam(exam);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newExamId = exam.getId();
        return ResultUtils.success(newExamId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteExam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Exam oldExam = examService.getById(id);
        ThrowUtils.throwIf(oldExam == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldExam.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = examService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param examUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateExam(@RequestBody ExamUpdateRequest examUpdateRequest) {
        if (examUpdateRequest == null || examUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Exam exam = new Exam();
        BeanUtils.copyProperties(examUpdateRequest, exam);

        // 参数校验
        examService.validExam(exam, false);
        long id = examUpdateRequest.getId();
        // 判断是否存在
        Exam oldExam = examService.getById(id);
        ThrowUtils.throwIf(oldExam == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = examService.updateById(exam);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ExamVO> getExamVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Exam exam = examService.getById(id);
        if (exam == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(examService.getExamVO(exam));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param examQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Exam>> listExamByPage(@RequestBody ExamQueryRequest examQueryRequest) {
        long current = examQueryRequest.getCurrent();
        long size = examQueryRequest.getPageSize();
        Page<Exam> examPage = examService.page(new Page<>(current, size),
                examService.getQueryWrapper(examQueryRequest));
        return ResultUtils.success(examPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param examQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ExamVO>> listExamVOByPage(@RequestBody ExamQueryRequest examQueryRequest,
            HttpServletRequest request) {
        long current = examQueryRequest.getCurrent();
        long size = examQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Exam> examPage = examService.page(new Page<>(current, size),
                examService.getQueryWrapper(examQueryRequest));
        return ResultUtils.success(examService.getExamVOPage(examPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param examQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ExamVO>> listMyExamVOByPage(@RequestBody ExamQueryRequest examQueryRequest,
            HttpServletRequest request) {
        if (examQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        examQueryRequest.setUserId(loginUser.getId());
        long current = examQueryRequest.getCurrent();
        long size = examQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Exam> examPage = examService.page(new Page<>(current, size),
                examService.getQueryWrapper(examQueryRequest));
        return ResultUtils.success(examService.getExamVOPage(examPage, request));
    }

    // endregion

    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param examQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/search/page/vo")
    public BaseResponse<Page<ExamVO>> searchExamVOByPage(@RequestBody ExamQueryRequest examQueryRequest,
            HttpServletRequest request) {
        long size = examQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Exam> examPage = examService.searchFromEs(examQueryRequest);
        return ResultUtils.success(examService.getExamVOPage(examPage, request));
    }

    /**
     * 编辑（用户）
     *
     * @param examEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editExam(@RequestBody ExamEditRequest examEditRequest, HttpServletRequest request) {
        if (examEditRequest == null || examEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Exam exam = new Exam();
        BeanUtils.copyProperties(examEditRequest, exam);

        // 参数校验
        examService.validExam(exam, false);
        User loginUser = userService.getLoginUser(request);
        long id = examEditRequest.getId();
        // 判断是否存在
        Exam oldExam = examService.getById(id);
        ThrowUtils.throwIf(oldExam == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldExam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = examService.updateById(exam);
        return ResultUtils.success(result);
    }



}
