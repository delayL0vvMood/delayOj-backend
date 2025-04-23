package com.fyy.delyoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.constant.CommonConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.exception.ThrowUtils;
import com.fyy.delyoj.model.dto.examuser.ExamUserQueryRequest;
import com.fyy.delyoj.model.entity.ExamUser;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.vo.ExamUserVO;
import com.fyy.delyoj.model.vo.UserVO;
import com.fyy.delyoj.service.ExamUserService;
import com.fyy.delyoj.mapper.ExamUserMapper;
import com.fyy.delyoj.service.UserService;
import com.fyy.delyoj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author fengyaoyang
* @description 针对表【exam_user(考试参与用户表)】的数据库操作Service实现
* @createDate 2025-03-10 16:00:02
*/
@Service
public class ExamUserServiceImpl extends ServiceImpl<ExamUserMapper, ExamUser>
    implements ExamUserService{

    @Resource
    private UserService userService;


    private static final Set<Integer> STATUS_ENUM_SET = new HashSet<>(Arrays.asList(0, 1, 2, 3));


    @Override
    public void validExamUser(ExamUser examUser, boolean add) {
        if (examUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        Long id = examUser.getId();
        Long examId = examUser.getExamId();
        Long userId = examUser.getUserId();
        Date joinTime = examUser.getJoinTime();
        Date submitTime = examUser.getSubmitTime();
        Integer status = examUser.getStatus();
        Date createTime = examUser.getCreateTime();
        Date updateTime = examUser.getUpdateTime();
        Integer isDelete = examUser.getIsDelete();



        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(ObjectUtils.allNotNull(examId, userId), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if(ObjectUtils.isNotEmpty(status) && !STATUS_ENUM_SET.contains(status)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"状态码错误");
        }
       
    }

    /**
     * 获取查询包装类
     * 用户根据字段查询，根据前端传来的请求对象，得到mybatis框架支持的queryWrapper类
     *
     *
     * @param examUserQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ExamUser> getQueryWrapper(ExamUserQueryRequest examUserQueryRequest) {
        QueryWrapper<ExamUser> queryWrapper = new QueryWrapper<>();
        if (examUserQueryRequest == null) {
            return queryWrapper;
        }
        Long id = examUserQueryRequest.getId();
        Long examId = examUserQueryRequest.getExamId();
        Long userId = examUserQueryRequest.getUserId();
        Date joinTime = examUserQueryRequest.getJoinTime();
        Date submitTime = examUserQueryRequest.getSubmitTime();
        Integer status = examUserQueryRequest.getStatus();
        String sortField = examUserQueryRequest.getSortField();
        String sortOrder = examUserQueryRequest.getSortOrder();
        
        

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(examId), "examId", examId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(joinTime), "joinTime", joinTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(submitTime),"submitTime",submitTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }

    @Override
    public Page<ExamUser> searchFromEs(ExamUserQueryRequest examUserQueryRequest) {
        return null;
    }


    /*
     *
     * 获取封装类
     * 查询创建人信息
     * */
    @Override
    public ExamUserVO getExamUserVO(ExamUser examUser) {
        
        ExamUserVO examUserVO = new ExamUserVO();
        
        long examUserId = examUser.getId();
        // 1. 关联查询用户信息
        Long userId = examUser.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        examUserVO.setUserVO(userVO);
        // 2. 已登录，获取用户点赞、收藏状态

        return examUserVO;
    }

    @Override
    public Page<ExamUserVO> getExamUserVOPage(Page<ExamUser> examUserPage, HttpServletRequest request) {
        List<ExamUser> examUserList = examUserPage.getRecords();
        Page<ExamUserVO> examUserVOPage = new Page<>(examUserPage.getCurrent(), examUserPage.getSize(), examUserPage.getTotal());
        if (CollectionUtils.isEmpty(examUserList)) {
            return examUserVOPage;
        }
/*        // 1. 关联查询用户信息
        Set<Long> userIdSet = examUserList.stream().map(ExamUser::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<ExamUserVO> examUserVOList = examUserList.stream().map(examUser -> {
            ExamUserVO examUserVO = ExamUserVO.objToVo(examUser);
            Long userId = examUser.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            examUserVO.setUserVO(userService.getUserVO(user));
            return examUserVO;
        }).collect(Collectors.toList());*/
        List<ExamUserVO> examUserVOList = examUserList.stream()
                .map(examUser -> getExamUserVO(examUser))
                .collect(Collectors.toList());
        examUserVOPage.setRecords(examUserVOList);
        return examUserVOPage;
    }

    @Override
    public List<Long> getExamUserUserIdList(ExamUserQueryRequest examUserQueryRequest){
        QueryWrapper<ExamUser> queryWrapper = getQueryWrapper(examUserQueryRequest);
        List<ExamUser> examUserList = list(queryWrapper);
        List<Long> examUserUserIdList = examUserList.stream()
                .map(examUser -> examUser.getUserId())
                .collect(Collectors.toList());
        return examUserUserIdList;
    }






}




