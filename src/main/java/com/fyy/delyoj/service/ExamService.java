package com.fyy.delyoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fyy.delyoj.model.dto.exam.ExamQueryRequest;
import com.fyy.delyoj.model.vo.ExamVO;
import com.fyy.delyoj.model.entity.Exam;

import javax.servlet.http.HttpServletRequest;

/**
* @author fengyaoyang
* @description 针对表【exam(考试表)】的数据库操作Service
* @createDate 2025-03-10 16:00:02
*/
public interface ExamService extends IService<Exam> {

    boolean saveExam(Exam exam);

    void validExam(Exam exam, boolean add);

    QueryWrapper<Exam> getQueryWrapper(ExamQueryRequest examQueryRequest);

    Page<Exam> searchFromEs(ExamQueryRequest examQueryRequest);

    ExamVO getExamVO(Exam exam);

    Page<ExamVO> getExamVOPage(Page<Exam> examPage, HttpServletRequest request);

    }
