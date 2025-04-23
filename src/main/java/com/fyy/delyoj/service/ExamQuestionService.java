package com.fyy.delyoj.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fyy.delyoj.model.dto.examquestion.ExamQuestionAddRequest;
import com.fyy.delyoj.model.dto.examquestion.ExamQuestionQueryRequest;
import com.fyy.delyoj.model.entity.ExamQuestion;
import com.fyy.delyoj.model.entity.Question;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.vo.ExamQuestionVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
* @author fengyaoyang
* @description 针对表【exam_question(考试题目关联表)】的数据库操作Service
* @createDate 2025-03-10 16:00:02
*/
public interface ExamQuestionService extends IService<ExamQuestion> {

    void vailExamQuestion(ExamQuestion examQuestion, boolean add);

    long addExamQuestion(ExamQuestionAddRequest examQuestionAddRequest, User loginUser);

    QueryWrapper<ExamQuestion> getQueryWrapper(ExamQuestionQueryRequest examQuestionQueryRequest);

    ExamQuestionVO getExamQuestionVO(ExamQuestion examQuestion, User loginUser);

    Page<ExamQuestionVO> getExamQuestionVOPage(Page<ExamQuestion> examQuestionPage, HttpServletRequest request);

    List<Long> getExamQuestionQuestionIdList(ExamQuestionQueryRequest examQuestionQueryRequest);
}
