package com.fyy.delyoj.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.constant.CommonConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.exception.ThrowUtils;
import com.fyy.delyoj.model.dto.userscore.UserScoreAddRequest;
import com.fyy.delyoj.model.dto.userscore.UserScoreQueryRequest;
import com.fyy.delyoj.model.entity.*;
import com.fyy.delyoj.model.enums.JudgeInfoMessageEnum;
import com.fyy.delyoj.model.vo.TotalScoreVO;
import com.fyy.delyoj.model.vo.UserScoreVO;
import com.fyy.delyoj.service.*;
import com.fyy.delyoj.mapper.UserScoreMapper;
import com.fyy.delyoj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author fengyaoyang
* @description 针对表【user_score(用户成绩表)】的数据库操作Service实现
* @createDate 2025-03-10 16:00:01
*/
@Service
public class UserScoreServiceImpl extends ServiceImpl<UserScoreMapper, UserScore>
    implements UserScoreService{
    
    @Resource
    QuestionService questionService;
    @Resource
    ExamService examService;
    @Resource
    UserService userService;

    @Resource
    @Lazy
    ExamRedisService examRedisService;

    /**
     * 点赞
     *
     * @param userScoreAddRequest
     * @return 提交记录id
     */
    @Override
    public long doUserScore(UserScoreAddRequest userScoreAddRequest, Long userId) {
        UserScore userScore = new UserScore();
        BeanUtils.copyProperties(userScoreAddRequest,userScore);
        userScore.setUserId(userId);
        validUserScore(userScore);

        Long examId = userScore.getExamId();
        Long questionId = userScore.getQuestionId();
        String judgeResult = userScore.getJudgeResult();
        Integer score = userScore.getScore();


        UserScoreQueryRequest userScoreQueryRequest = new UserScoreQueryRequest();
        userScoreQueryRequest.setExamId(examId);
        userScoreQueryRequest.setQuestionId(questionId);
        userScoreQueryRequest.setUserId(userId);
        userScoreQueryRequest.setCurrent(1);
        userScoreQueryRequest.setPageSize(1);
        QueryWrapper<UserScore> queryWrapper = getQueryWrapper(userScoreQueryRequest);
        queryWrapper.last("LIMIT 1");
        UserScore singleData = getOne(queryWrapper);
        boolean hasDataBySingle = singleData != null;

        if(hasDataBySingle) {//判断是否成功提交过分数,如果提交过且提交是ACCEPT，就更新信息
            LambdaUpdateWrapper<UserScore> userScoreUpdate = new LambdaUpdateWrapper<>();
            userScoreUpdate.eq(UserScore::getExamId, examId)
                    .eq(UserScore::getQuestionId, questionId)
                    .eq(UserScore::getUserId, userId)
                    .set(UserScore::getJudgeResult,judgeResult)
                    .set(UserScore::getScore, score);
            boolean update = this.update(null, userScoreUpdate);
            if(!update){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
            }
        }
        else if(!hasDataBySingle) {
            //没提交过
            boolean save = this.save(userScore);
            if(!save){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"提交分数失败");
            }
        }
        examRedisService.updateScore(
                examId,
                userId,
                questionId,
                score
        );
        return ObjectUtils.isNotEmpty(userScore.getId())?userScore.getId():singleData.getId();
    }
    
    @Override
    public void validUserScore(UserScore userScore) {
        if (userScore == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = userScore.getId();
        Long examId = userScore.getExamId();
        Long userId = userScore.getUserId();
        Long questionId = userScore.getQuestionId();
        Integer score = userScore.getScore();

        // 参数不能为空
        ThrowUtils.throwIf(ObjectUtils.anyNull(examId, userId, questionId), ErrorCode.PARAMS_ERROR);
        
        Question question = questionService.getById(questionId);
        Exam exam = examService.getById(examId);
        User user = userService.getById(userId);
        ThrowUtils.throwIf(ObjectUtils.anyNull(question,exam,user),ErrorCode.PARAMS_ERROR);

        
        
        if (ObjectUtils.isNotEmpty(score) && score > 1000 && score <0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"分数超出限制");
        }
    }


    /**
     * 获取查询包装类
     * 用户根据字段查询，根据前端传来的请求对象，得到mybatis框架支持的queryWrapper类
     *
     *
     * @param userScoreQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserScore> getQueryWrapper(UserScoreQueryRequest userScoreQueryRequest) {
        QueryWrapper<UserScore> queryWrapper = new QueryWrapper<>();
        if (userScoreQueryRequest == null) {
            return queryWrapper;
        }
        Long id = userScoreQueryRequest.getId();
        Long examId = userScoreQueryRequest.getExamId();
        Long userId = userScoreQueryRequest.getUserId();
        Long questionId = userScoreQueryRequest.getQuestionId();
        Integer score = userScoreQueryRequest.getScore();
        String judgeResult = userScoreQueryRequest.getJudgeResult();
        int current = userScoreQueryRequest.getCurrent();
        int pageSize = userScoreQueryRequest.getPageSize();
        String sortField = userScoreQueryRequest.getSortField();
        String sortOrder = userScoreQueryRequest.getSortOrder();
        

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(examId),"examId",examId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId),"questionId",questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(judgeResult),"judgeResult",judgeResult);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }

    @Override
    public Page<UserScoreVO> getUserScoreVOPage(Page<UserScore> userScorePage) {
        List<UserScore> userScoreList = userScorePage.getRecords();
        Page<UserScoreVO> userScoreVOPage = new Page<>(userScorePage.getCurrent(), userScorePage.getSize(), userScorePage.getTotal());
        if (CollectionUtils.isEmpty(userScoreList)) {
            return userScoreVOPage;
        }
        List<UserScoreVO> userScoreVOList = userScoreList.stream()
                .map(userScore -> {
                    UserScoreVO userScoreVO = new UserScoreVO();
                    BeanUtils.copyProperties(userScore,userScoreVO);
                    return userScoreVO;
                })
                .collect(Collectors.toList());
        userScoreVOPage.setRecords(userScoreVOList);

        userScoreVOPage.setRecords(userScoreVOList);
        return userScoreVOPage;

    }


    @Override
    public Page<TotalScoreVO> getTotalScore(UserScoreQueryRequest userScoreQueryRequest) {

        Long examId = userScoreQueryRequest.getExamId();
        Long userId = userScoreQueryRequest.getUserId();
        int current = userScoreQueryRequest.getCurrent();
        int pageSize = userScoreQueryRequest.getPageSize();

        ThrowUtils.throwIf(ObjectUtils.isEmpty(examId),ErrorCode.PARAMS_ERROR);
        QueryWrapper<UserScore> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("examId",examId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);

        List<UserScore> userScoreList = list(queryWrapper);


        Map<Long, List<UserScore>> userIdUserScoreMap = userScoreList.stream()
                .collect(Collectors.groupingBy(UserScore::getUserId));

        List<TotalScoreVO> totalScoreVOList = userIdUserScoreMap.entrySet().stream()
                .map(longListEntry -> {
                    TotalScoreVO totalScoreVO = new TotalScoreVO();
                    List<UserScore> scoreList = longListEntry.getValue();
                    Double totalScore = .0;
                    for (UserScore userScore : scoreList) totalScore += userScore.getScore();
                    totalScoreVO.setUserId(longListEntry.getKey());
                    totalScoreVO.setTotalScore(totalScore);
                    return totalScoreVO;
                })
                .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
                .collect(Collectors.toList());

        Page<TotalScoreVO> totalScoreVOPage = new Page<>();
        totalScoreVOPage.setRecords(totalScoreVOList);
        totalScoreVOPage.setTotal(totalScoreVOList.size());
        totalScoreVOPage.setSize(pageSize);
        totalScoreVOPage.setCurrent(current);

        return totalScoreVOPage;
    }



}




