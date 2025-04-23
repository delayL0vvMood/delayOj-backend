package com.fyy.delyoj.service.impl;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fyy.delyoj.model.dto.examquestion.ExamQuestionQueryRequest;
import com.fyy.delyoj.model.dto.examuser.ExamUserQueryRequest;
import com.fyy.delyoj.model.dto.userscore.UserScoreQueryRequest;
import com.fyy.delyoj.model.entity.Exam;
import com.fyy.delyoj.model.entity.UserScore;
import com.fyy.delyoj.model.enums.RedisKeysEnum;
import com.fyy.delyoj.model.vo.TotalScoreVO;
import com.fyy.delyoj.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamRedisServiceImpl implements ExamRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserScoreService userScoreService;

    @Resource
    private ExamUserService examUserService;

    @Resource
    private ExamQuestionService examQuestionService;

    private static final DefaultRedisScript<Long> UPDATE_EXAM_SCRIPT;
    static {
        UPDATE_EXAM_SCRIPT = new DefaultRedisScript<>();
        UPDATE_EXAM_SCRIPT.setLocation(new ClassPathResource("lua/update_score.lua"));
        UPDATE_EXAM_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private UserScoreServiceImpl userScoreServiceImpl;

    //原子化更新分数
    @Override
    public void updateScore(Long examId, Long userId, Long questionId, Integer newScore) {
        List<String> keys = Arrays.asList(
                RedisKeysEnum.examRankKey(examId),
                RedisKeysEnum.userScoreKey(examId,userId)
        );
        redisTemplate.execute(
                UPDATE_EXAM_SCRIPT,
                keys,
                questionId.toString(),
                newScore,
                userId.toString()
        );


    }


    //总分+题目正确情况分页
    @Override
    public Page<TotalScoreVO> getRedisRankPage(UserScoreQueryRequest userScoreQueryRequest) {

        Long examId = userScoreQueryRequest.getExamId();
        int current = userScoreQueryRequest.getCurrent();
        int pageSize = userScoreQueryRequest.getPageSize();


        String rankKey = RedisKeysEnum.examRankKey(examId);

        // 获取总数
        Long total = redisTemplate.opsForZSet().size(rankKey);
        if (total == null) total = 0L;

        // 分页查询
        long start = (current - 1) * pageSize;
        long end = start + pageSize - 1;
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankKey, start, end);

        // 转换为VO
        List<TotalScoreVO> records = tuples.stream().map(tuple -> {
            TotalScoreVO vo = new TotalScoreVO();
            vo.setExamId(examId);
            vo.setUserId(Long.parseLong(tuple.getValue().toString()));
            vo.setTotalScore(tuple.getScore());
            return vo;
        }).collect(Collectors.toList());

        // 补充题目详情
        records.forEach(vo -> {
            Map<Object, Object> details = redisTemplate.opsForHash()
                    .entries(RedisKeysEnum.userScoreKey(examId, vo.getUserId()));
            String JSONDetails = JSONUtil.toJsonStr(details);
            vo.setQuestionScores(JSONDetails);
        });

        Page<TotalScoreVO> page = new Page<>(current, pageSize);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    //初始化排名
    @Override
    public void initExamRank(Exam exam){
        Long examId = exam.getId();
        //初始化考试排名
        ExamUserQueryRequest examUserQueryRequest = new ExamUserQueryRequest();
        examUserQueryRequest.setExamId(examId);
        String rankKey = RedisKeysEnum.examRankKey(examId);
        //从数据库加载数据
        List<Long> examUserUserIdList = examUserService
                .getExamUserUserIdList(examUserQueryRequest);

        ExamQuestionQueryRequest examQuestionQueryRequest = new ExamQuestionQueryRequest();
        examQuestionQueryRequest.setExamId(examId);
        List<Long> examQuestionQuestionIdList = examQuestionService.getExamQuestionQuestionIdList(examQuestionQueryRequest);

        redisTemplate.executePipelined((RedisCallback<Void>) connection ->{
            examUserUserIdList.forEach(userId ->{
                String userKey = RedisKeysEnum.userScoreKey(examId, userId);

                Map<byte[], byte[]> userScores = examQuestionQuestionIdList.stream()
                        .collect(Collectors.toMap(
                                s -> s.toString().getBytes(),
                                s -> {
                                    Double score = .0;
                                    return score.toString().getBytes();
                                }
                        ));
                connection.hMSet(userKey.getBytes(), userScores);

                connection.zAdd(rankKey.getBytes(),.0, userId.toString().getBytes());
            });
            return null;
        });
        // 4. 设置过期时间
        if (exam != null) {
            long currentMillis = System.currentTimeMillis();
            long endMillis = exam.getEndTime().getTime();
            long ttl = ((endMillis - currentMillis) / 1000) + 86400;
            if (ttl > 0) {
                redisTemplate.expire(rankKey, ttl, TimeUnit.SECONDS);
            }
        }
    }




    //从数据库同步考试排名数据
    @Override
    public void initExamData(Exam exam) {
        Long examId = exam.getId();

        // 1. 删除旧数据
        String rankKey = RedisKeysEnum.examRankKey(examId);
        redisTemplate.delete(rankKey);

        // 2. 从数据库加载数据
        List<UserScore> allScores = userScoreService.list(
                new QueryWrapper<UserScore>()
                        .eq("examId", examId)
                        .eq("isDelete", 0)
        );

        // 3. 使用管道批量插入
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            // 按用户分组
            Map<Long, List<UserScore>> userScores = allScores.stream()
                    .collect(Collectors.groupingBy(UserScore::getUserId));

            userScores.forEach((userId, scores) -> {
                // 用户题目得分Hash
                String userKey = RedisKeysEnum.userScoreKey(examId, userId);
                Map<byte[], byte[]> questionMap = scores.stream()
                        .collect(Collectors.toMap(
                                s -> s.getQuestionId().toString().getBytes(),
                                s -> s.getScore().toString().getBytes()
                        ));
                connection.hMSet(userKey.getBytes(), questionMap);

                // 计算总分
                double total = scores.stream()
                        .mapToInt(UserScore::getScore)
                        .sum();
                connection.zAdd(rankKey.getBytes(), total, userId.toString().getBytes());
            });
            return null;
        });

        // 4. 设置过期时间
        if (exam != null) {
            long currentMillis = System.currentTimeMillis();
            long endMillis = exam.getEndTime().getTime();
            long ttl = ((endMillis - currentMillis) / 1000) + 86400;
            if (ttl > 0) {
                redisTemplate.expire(rankKey, ttl, TimeUnit.SECONDS);
            }
        }
    }
}
