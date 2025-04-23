package com.fyy.delyoj.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.constant.CommonConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.judge.JudgeService;
import com.fyy.delyoj.model.dto.examsubmit.ExamSubmitAddRequest;
import com.fyy.delyoj.model.dto.examsubmit.ExamSubmitQueryRequest;
import com.fyy.delyoj.model.dto.message.JudgeMessage;
import com.fyy.delyoj.model.dto.message.JudgeMessageEnum;
import com.fyy.delyoj.model.entity.*;
import com.fyy.delyoj.model.enums.QuestionSubmitStatusEnum;
import com.fyy.delyoj.model.enums.QuestionSubmitLanguageEnum;
import com.fyy.delyoj.model.vo.ExamSubmitVO;
import com.fyy.delyoj.rabbitmq.MessageProducer;
import com.fyy.delyoj.service.*;
import com.fyy.delyoj.mapper.ExamSubmitMapper;
import com.fyy.delyoj.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* @author fengyaoyang
* @description 针对表【exam_submit(考试提交记录表)】的数据库操作Service实现
* @createDate 2025-03-10 16:00:02
*/
@Service
public class ExamSubmitServiceImpl extends ServiceImpl<ExamSubmitMapper, ExamSubmit>
    implements ExamSubmitService{

    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private MessageProducer messageProducer;

    /*
     * ExamSubmitService和JudgeService相互调用，相互依赖，SpringBoot创建bean类时，
     * 会先创建ExamSubmitService，在创建JudgeService时，会调用ExamSubmitService，
     * 此时ExamSubmitService还未创建完成，导致报错
     * 使用@Lazy注解，延迟加载ExamSubmitService，在创建JudgeService时，不会创建ExamSubmitService
     * */
    @Resource
    @Lazy
    private JudgeService judgeService;


    /**
     * 点赞
     *
     * @param examSubmitAddRequest
     * @param loginUser
     * @return 提交记录id
     */
    @Override
    public long doExamSubmit(ExamSubmitAddRequest examSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = examSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum enumByValue = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言不合法");
        }
        // 判断实体是否存在，根据类别获取实体
        long questionId = examSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
       /* // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        ExamSubmitService examSubmitService = (ExamSubmitService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            *//*防止重复提交，连点n次，数据库只提交1条*//*
            return examSubmitService.doExamSubmitInner(userId, questionId);
        }*/

        ExamSubmit examSubmit = new ExamSubmit();
        examSubmit.setExamId(examSubmitAddRequest.getExamId());
        examSubmit.setUserId(userId);
        examSubmit.setQuestionId(questionId);
        examSubmit.setCode(examSubmitAddRequest.getCode());
        examSubmit.setLanguage(examSubmitAddRequest.getLanguage());

        //  设置初始状态
        examSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());//获取初始状态
        examSubmit.setJudgeInfo("{}");
        boolean save = this.save(examSubmit);


        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(examSubmitAddRequest.getCode());
        questionSubmit.setLanguage(examSubmitAddRequest.getLanguage());

        //  设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());//获取初始状态
        questionSubmit.setJudgeInfo("{}");
        boolean saveQuestion = questionSubmitService.save(questionSubmit);
        if (!save || !saveQuestion) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交失败");
        }else {
            LambdaUpdateWrapper<Question> questionLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            questionLambdaUpdateWrapper.eq(Question::getId,questionId)
                    .setSql("submitNum = submitNum + 1");
            questionService.update(null,questionLambdaUpdateWrapper);
        }

        // 执行判题服务 异步处理：
        Long questionSubmitId = questionSubmit.getId();
        Long examSubmitId = examSubmit.getId();


        JudgeMessage judgeMessage = new JudgeMessage();
        judgeMessage.setMessageType(JudgeMessageEnum.EXAM_SUBMIT);
        judgeMessage.setQuestionSubmitId(questionSubmitId);
        judgeMessage.setExamSubmitId(examSubmitId);
        judgeMessage.setUserId(userId);

        // 发送消息到消息队列
        messageProducer.sendMessage("code_exchange", "my_routingKey", judgeMessage);
//        CompletableFuture.runAsync(() -> {
//            judgeService.doExamJudge(questionSubmitId, examSubmitId, userId);
//        });
        return examSubmit.getId();

    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param questionId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doExamSubmitInner(long userId, long questionId) {
        ExamSubmit examSubmit = new ExamSubmit();
        examSubmit.setUserId(userId);
        examSubmit.setQuestionId(questionId);
        QueryWrapper<ExamSubmit> thumbQueryWrapper = new QueryWrapper<>(examSubmit);
        ExamSubmit oldExamSubmit = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldExamSubmit != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = questionService.update()
                        .eq("id", questionId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(examSubmit);
            if (result) {
                // 点赞数 + 1
                result = questionService.update()
                        .eq("id", questionId)
                        .setSql("thumbNum = thumbNum + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }


    /**
     * 查询提交记录
     *
     * @param examSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ExamSubmit> getQueryWrapper(ExamSubmitQueryRequest examSubmitQueryRequest) {
        QueryWrapper<ExamSubmit> queryWrapper = new QueryWrapper<>();
        if (examSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = examSubmitQueryRequest.getLanguage();
        Integer status = examSubmitQueryRequest.getStatus();
        Long questionId = examSubmitQueryRequest.getQuestionId();
        Long userId = examSubmitQueryRequest.getUserId();
        Long examId = examSubmitQueryRequest.getExamId();
        String sortField = examSubmitQueryRequest.getSortField();
        String sortOrder = examSubmitQueryRequest.getSortOrder();

        //拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(examId),"examId", examId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId),"questionId",questionId);
        //
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;

    }

    /**
     * 获取提交封装
     */
    @Override
    public ExamSubmitVO getExamSubmitVO(ExamSubmit examSubmit, User loginUser) {
        ExamSubmitVO examSubmitVO = ExamSubmitVO.objToVo(examSubmit);
        /*
         * 得到未脱敏数据
         * */

        /*
         * 每次调用函数，都会获取用户信息，造成性能浪费
         * User loginUser = userService.getLoginUser(request);
         * */
        long userId = loginUser.getId();
        /*
         * 如果查询者非题目提交本人
         * 用户id不等于题目提交用户id'
         * 返回信息脱敏 (无法查看代码)
         * */
        if (userId != examSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            examSubmitVO.setCode(null);

        }

        return examSubmitVO;
    }


    /**
     * 分页获取题目封装
     *
     * @param examSubmitPage
     * @param loginUser
     * @return
     */
    @Override
    public Page<ExamSubmitVO> getExamSubmitVOPage(Page<ExamSubmit> examSubmitPage, User loginUser) {
        List<ExamSubmit> examSubmitList = examSubmitPage.getRecords();
        Page<ExamSubmitVO> examSubmitVOPage = new Page<>(examSubmitPage.getCurrent(), examSubmitPage.getSize(), examSubmitPage.getTotal());
        if (CollectionUtil.isEmpty(examSubmitList)) {
            return examSubmitVOPage;
        }
/*
        //先将用户id放到列表当中，根据多条id查用户表，得到用户集合，根据id进行分组，得到每个id对应的用户信息

        Set<Long> userIdSet = examSubmitList.stream().map(ExamSubmit::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));

        //封装用户信息

         //根据id与原有的用户信息进行匹配，将原有的用户信息填充到问题表中

        List<ExamSubmitVO> examSubmitVOList = examSubmitList.stream().map(examSubmit -> {
            ExamSubmitVO examSubmitVO = ExamSubmitVO.objToVo(examSubmit);
            Long userId = examSubmit.getUserId();
            User user = null;
            if(userIdUserListMap.containsKey(userId)){
                user = userIdUserListMap.get(userId).get(0);
            }
            examSubmitVO.setUserVO(userService.getUserVO(user));
            return examSubmitVO;
        }).collect(Collectors.toList());

        */
        List<ExamSubmitVO> examSubmitVOList = examSubmitList.stream()
                .map(examSubmit -> getExamSubmitVO(examSubmit, loginUser))
                .collect(Collectors.toList());
        examSubmitVOPage.setRecords(examSubmitVOList);
        return examSubmitVOPage;
    }

}




