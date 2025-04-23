package com.fyy.delyoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fyy.delyoj.model.dto.examuser.ExamUserQueryRequest;
import com.fyy.delyoj.model.entity.ExamUser;
import com.fyy.delyoj.model.vo.ExamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author fengyaoyang
* @description 针对表【exam_user(考试参与用户表)】的数据库操作Service
* @createDate 2025-03-10 16:00:02
*/
public interface ExamUserService extends IService<ExamUser> {

    void validExamUser(ExamUser examUser, boolean add);

    QueryWrapper<ExamUser> getQueryWrapper(ExamUserQueryRequest examUserQueryRequest);

    Page<ExamUser> searchFromEs(ExamUserQueryRequest examUserQueryRequest);

    /*
     *
     * 获取封装类
     * 查询创建人信息
     * */
    ExamUserVO getExamUserVO(ExamUser examUser);

    Page<ExamUserVO> getExamUserVOPage(Page<ExamUser> examUserPage, HttpServletRequest request);

    List<Long> getExamUserUserIdList(ExamUserQueryRequest examUserQueryRequest);
}
