package com.fyy.delyoj.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fyy.delyoj.model.dto.examsubmit.ExamSubmitAddRequest;
import com.fyy.delyoj.model.dto.examsubmit.ExamSubmitQueryRequest;
import com.fyy.delyoj.model.entity.ExamSubmit;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.vo.ExamSubmitVO;
import org.springframework.transaction.annotation.Transactional;

/**
* @author fengyaoyang
* @description 针对表【exam_submit(考试提交记录表)】的数据库操作Service
* @createDate 2025-03-10 16:00:02
*/
public interface ExamSubmitService extends IService<ExamSubmit> {

    long doExamSubmit(ExamSubmitAddRequest examSubmitAddRequest, User loginUser);

    @Transactional(rollbackFor = Exception.class)
    int doExamSubmitInner(long userId, long questionId);

    QueryWrapper<ExamSubmit> getQueryWrapper(ExamSubmitQueryRequest examSubmitQueryRequest);

    ExamSubmitVO getExamSubmitVO(ExamSubmit examSubmit, User loginUser);

    Page<ExamSubmitVO> getExamSubmitVOPage(Page<ExamSubmit> examSubmitPage, User loginUser);
}
