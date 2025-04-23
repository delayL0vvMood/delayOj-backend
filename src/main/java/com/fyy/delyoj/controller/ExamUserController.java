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
import com.fyy.delyoj.model.dto.examuser.ExamUserAddRequest;
import com.fyy.delyoj.model.dto.examuser.ExamUserEditRequest;
import com.fyy.delyoj.model.dto.examuser.ExamUserQueryRequest;
import com.fyy.delyoj.model.dto.examuser.ExamUserUpdateRequest;
import com.fyy.delyoj.model.entity.ExamUser;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.vo.ExamUserVO;
import com.fyy.delyoj.service.ExamUserService;
import com.fyy.delyoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/exam/user")
@Slf4j
public class ExamUserController {

    @Resource
    private ExamUserService examUserService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param examUserAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addExamUser(@RequestBody ExamUserAddRequest examUserAddRequest, HttpServletRequest request) {
        if (examUserAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ExamUser examUser = new ExamUser();
        BeanUtils.copyProperties(examUserAddRequest, examUser);

        examUserService.validExamUser(examUser, true);
        User loginUser = userService.getLoginUser(request);
        examUser.setUserId(loginUser.getId());
        boolean result = examUserService.save(examUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newExamUserId = examUser.getId();
        return ResultUtils.success(newExamUserId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteExamUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ExamUser oldExamUser = examUserService.getById(id);
        ThrowUtils.throwIf(oldExamUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldExamUser.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = examUserService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员和本人）
     *
     * @param examUserUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateExamUser(@RequestBody ExamUserUpdateRequest examUserUpdateRequest) {
        if (examUserUpdateRequest == null || examUserUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ExamUser examUser = new ExamUser();
        BeanUtils.copyProperties(examUserUpdateRequest, examUser);
        // 参数校验
        examUserService.validExamUser(examUser, false);
        long id = examUserUpdateRequest.getId();
        // 判断是否存在
        ExamUser oldExamUser = examUserService.getById(id);
        ThrowUtils.throwIf(oldExamUser == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = examUserService.updateById(examUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ExamUserVO> getExamUserVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ExamUser examUser = examUserService.getById(id);
        if (examUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(examUserService.getExamUserVO(examUser));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param examUserQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ExamUser>> listExamUserByPage(@RequestBody ExamUserQueryRequest examUserQueryRequest) {
        long current = examUserQueryRequest.getCurrent();
        long size = examUserQueryRequest.getPageSize();
        Page<ExamUser> examUserPage = examUserService.page(new Page<>(current, size),
                examUserService.getQueryWrapper(examUserQueryRequest));
        return ResultUtils.success(examUserPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param examUserQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ExamUserVO>> listExamUserVOByPage(@RequestBody ExamUserQueryRequest examUserQueryRequest,
            HttpServletRequest request) {
        long current = examUserQueryRequest.getCurrent();
        long size = examUserQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ExamUser> examUserPage = examUserService.page(new Page<>(current, size),
                examUserService.getQueryWrapper(examUserQueryRequest));
        return ResultUtils.success(examUserService.getExamUserVOPage(examUserPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param examUserQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ExamUserVO>> listMyExamUserVOByPage(@RequestBody ExamUserQueryRequest examUserQueryRequest,
            HttpServletRequest request) {
        if (examUserQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        examUserQueryRequest.setUserId(loginUser.getId());
        long current = examUserQueryRequest.getCurrent();
        long size = examUserQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ExamUser> examUserPage = examUserService.page(new Page<>(current, size),
                examUserService.getQueryWrapper(examUserQueryRequest));
        return ResultUtils.success(examUserService.getExamUserVOPage(examUserPage, request));
    }

    // endregion

    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param examUserQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/search/page/vo")
    public BaseResponse<Page<ExamUserVO>> searchExamUserVOByPage(@RequestBody ExamUserQueryRequest examUserQueryRequest,
            HttpServletRequest request) {
        long size = examUserQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ExamUser> examUserPage = examUserService.searchFromEs(examUserQueryRequest);
        return ResultUtils.success(examUserService.getExamUserVOPage(examUserPage, request));
    }

    /**
     * 编辑（用户）
     *
     * @param examUserEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editExamUser(@RequestBody ExamUserEditRequest examUserEditRequest, HttpServletRequest request) {
        if (examUserEditRequest == null || examUserEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ExamUser examUser = new ExamUser();
        BeanUtils.copyProperties(examUserEditRequest, examUser);

        // 参数校验
        examUserService.validExamUser(examUser, false);
        User loginUser = userService.getLoginUser(request);
        long id = examUserEditRequest.getId();
        // 判断是否存在
        ExamUser oldExamUser = examUserService.getById(id);
        ThrowUtils.throwIf(oldExamUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldExamUser.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = examUserService.updateById(examUser);
        return ResultUtils.success(result);
    }

}
