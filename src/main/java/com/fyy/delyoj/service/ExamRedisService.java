package com.fyy.delyoj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fyy.delyoj.model.dto.userscore.UserScoreQueryRequest;
import com.fyy.delyoj.model.entity.Exam;
import com.fyy.delyoj.model.vo.TotalScoreVO;

import java.util.List;

public interface ExamRedisService {
    //原子化更新分数
    void updateScore(Long examId, Long userId, Long questionId, Integer newScore);

    Page<TotalScoreVO> getRedisRankPage(UserScoreQueryRequest userScoreQueryRequest);

    void initExamRank(Exam exam);

    void initExamData(Exam exam);
}
