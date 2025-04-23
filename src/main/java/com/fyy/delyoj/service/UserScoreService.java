package com.fyy.delyoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fyy.delyoj.model.dto.userscore.UserScoreAddRequest;
import com.fyy.delyoj.model.dto.userscore.UserScoreQueryRequest;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.entity.UserScore;
import com.fyy.delyoj.model.vo.TotalScoreVO;
import com.fyy.delyoj.model.vo.UserScoreVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author fengyaoyang
* @description 针对表【user_score(用户成绩表)】的数据库操作Service
* @createDate 2025-03-10 16:00:02
*/
public interface UserScoreService extends IService<UserScore> {

    void validUserScore(UserScore userScore);

    long doUserScore(UserScoreAddRequest userScoreAddRequest, Long userId);

    QueryWrapper<UserScore> getQueryWrapper(UserScoreQueryRequest userScoreQueryRequest);

    Page<UserScoreVO> getUserScoreVOPage(Page<UserScore> userScorePage);

    Page<TotalScoreVO> getTotalScore(UserScoreQueryRequest userScoreQueryRequest);
}
