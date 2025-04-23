package com.fyy.delyoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fyy.delyoj.common.BaseResponse;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.common.ResultUtils;
import com.fyy.delyoj.constant.CommonConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.exception.ThrowUtils;
import com.fyy.delyoj.model.dto.userscore.UserScoreAddRequest;
import com.fyy.delyoj.model.dto.userscore.UserScoreQueryRequest;
import com.fyy.delyoj.model.entity.Exam;
import com.fyy.delyoj.model.entity.UserScore;
import com.fyy.delyoj.model.entity.User;
import com.fyy.delyoj.model.enums.RedisKeysEnum;
import com.fyy.delyoj.model.vo.TotalScoreVO;
import com.fyy.delyoj.model.vo.UserScoreVO;
import com.fyy.delyoj.service.ExamRedisService;
import com.fyy.delyoj.service.ExamService;
import com.fyy.delyoj.service.UserScoreService;
import com.fyy.delyoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @description 用户成绩表Service实现
 *
 */
@RestController
@RequestMapping("/user/score")
@Slf4j
public class UserScoreController {

    @Resource
    private UserScoreService userScoreService;

    @Resource
    private UserService userService;

    @Resource
    private ExamService examService;


    @Resource
    ExamRedisService examRedisService;

    @Resource
    RedisTemplate<String, Object> redisTemplate;
/*    *
     * 提交题目
     *
     * @param userScoreAddRequest
     * @param request
     */
    @PostMapping("/")
    @Deprecated
    public BaseResponse<Long> doSubmit(@RequestBody UserScoreAddRequest userScoreAddRequest,
                                       HttpServletRequest request) {
        if (userScoreAddRequest == null || userScoreAddRequest.getQuestionId() <= 0 || userScoreAddRequest.getExamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long questionId = userScoreAddRequest.getQuestionId();
        long userScoreId = userScoreService.doUserScore(userScoreAddRequest, loginUser.getId());
        return ResultUtils.success(userScoreId);
    }



/*    *
     * 分页获取题目提交列表（仅管理员，用户能查看到除答案的列表）
     * 根据权限过滤答案
     * 功能：根据用户id，题目id，编程语言，题目状态，分页查询
     * @param userScoreQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<UserScoreVO>> listUserScoreByPage(@RequestBody UserScoreQueryRequest userScoreQueryRequest, HttpServletRequest request) {
        long current = userScoreQueryRequest.getCurrent();
        long size = userScoreQueryRequest.getPageSize();

        
/*        * 先查出所有题目提交，然后根据用户id，题目id，编程语言，题目状态，分页查询
        * 从数据库中查到原始分页信息
        * */
        Page<UserScore> userScorePage = userScoreService.page(new Page<>(current, size),
                userScoreService.getQueryWrapper(userScoreQueryRequest));

        final User loginUser = userService.getLoginUser(request);
        /*
         * 脱敏，封装类
         * */
        return ResultUtils.success(userScoreService
                .getUserScoreVOPage(userScorePage));
    }


    /*
    *
    * 分页获取考试总成绩
    *  @param userScoreQueryRequest
    *
    * */

    @PostMapping("/list/total_score")
    public BaseResponse<Page<TotalScoreVO>> listTotalScoreByPage(@RequestBody UserScoreQueryRequest userScoreQueryRequest, HttpServletRequest request) {

        Long examId = userScoreQueryRequest.getExamId();
        boolean useOrNot = shouldUseRedis(examId);
        Page<TotalScoreVO> totalScore;
        if (useOrNot) {
            totalScore = examRedisService.getRedisRankPage(userScoreQueryRequest);
        } else{
            totalScore = userScoreService.getTotalScore(userScoreQueryRequest);
        }
        return ResultUtils.success(totalScore);
    }

    // 判断是否使用Redis
    private boolean shouldUseRedis(Long examId) {
        try {
            String rankKey = RedisKeysEnum.examRankKey(examId);
            Boolean hasKey = redisTemplate.hasKey(rankKey);

            // 如果存在Key且未过期
            if (Boolean.TRUE.equals(hasKey)) {
                return true;
            }

            // 检查考试状态（需要实现）
            Exam exam = examService.getById(examId);
            if (exam == null) return false;

            // 如果考试还在进行中，尝试初始化Redis
            if (isExamActive(exam)) {
                //分布式锁防止重复初始化
                String lockKey = RedisKeysEnum.ExamLockKey(examId);
                Boolean locked = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
                if (Boolean.TRUE.equals(locked)) {
                    try {
                        initRedisDataIfNeeded(examId);
                    }finally {
                        redisTemplate.delete(lockKey);
                    }
                }
                return redisTemplate.hasKey(rankKey);
            }

            return false;
        } catch (Exception e) {
            log.error("Redis检查失败，降级到数据库", e);
            return false;
        }
    }
    // 考试是否进行中（示例实现）
    private boolean isExamActive(Exam exam) {
        //todo 修改为判断考试状态
        return true;
    }

    private void initRedisDataIfNeeded(Long examId) {
        String rankKey = RedisKeysEnum.examRankKey(examId);
        Exam exam = examService.getById(examId);
        if (!redisTemplate.hasKey(rankKey)) {
            // 异步初始化防止阻塞请求
            CompletableFuture.runAsync(() -> {
                try {
                    examRedisService.initExamData(exam);
                    // 设置过期时间为考试结束后24小时
                    redisTemplate.expire(rankKey, 24, TimeUnit.HOURS);
                } catch (Exception e) {
                    log.error("考试{}初始化Redis失败", examId, e);
                }
            });
        }
    }
}
