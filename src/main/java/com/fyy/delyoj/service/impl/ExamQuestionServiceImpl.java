package com.fyy.delyoj.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.constant.CommonConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.exception.ThrowUtils;
import com.fyy.delyoj.judge.JudgeService;
import com.fyy.delyoj.model.dto.examquestion.ExamQuestionAddRequest;
import com.fyy.delyoj.model.dto.examquestion.ExamQuestionQueryRequest;
import com.fyy.delyoj.model.entity.*;
import com.fyy.delyoj.model.vo.ExamQuestionVO;
import com.fyy.delyoj.service.*;
import com.fyy.delyoj.mapper.ExamQuestionMapper;
import com.fyy.delyoj.service.ExamQuestionService;
import com.fyy.delyoj.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.fyy.delyoj.model.entity.ExamQuestion;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author fengyaoyang
* @description 针对表【exam_question(考试题目关联表)】的数据库操作Service实现
* @createDate 2025-03-10 16:00:02
*/
@Service
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion>
    implements ExamQuestionService{

    @Resource
    private UserService userService;

    @Resource
    private  ExamService examService;

    @Resource
    private QuestionService questionService;



    /*
     * ExamQuestionService和JudgeService相互调用，相互依赖，SpringBoot创建bean类时，
     * 会先创建ExamQuestionService，在创建JudgeService时，会调用ExamQuestionService，
     * 此时ExamQuestionService还未创建完成，导致报错
     * 使用@Lazy注解，延迟加载ExamQuestionService，在创建JudgeService时，不会创建ExamQuestionService
     * */
    @Resource
    @Lazy
    private JudgeService judgeService;

    @Override
    public void vailExamQuestion(ExamQuestion examQuestion, boolean add){
        if(examQuestion == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = examQuestion.getId();
        Long examId = examQuestion.getExamId();
        Long questionId = examQuestion.getQuestionId();
        Integer score = examQuestion.getScore();
        Integer questionOrder = examQuestion.getQuestionOrder();


        if(add) {
            ThrowUtils.throwIf(ObjectUtils.anyNull(examId,questionId,score,questionOrder) , ErrorCode.PARAMS_ERROR);
        }

        if(ObjectUtils.isNotEmpty(examId) && examService.getById(examId) == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未找到考试");
        }
        if(ObjectUtils.isNotEmpty(questionId) && questionService.getById(questionId) == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到题目");
        }
        if(ObjectUtils.isNotEmpty(score) && (score>1000 || score <= 0)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分数超出范围");
        }
        if(ObjectUtils.isNotEmpty(questionOrder) && (questionOrder >=100 ||questionOrder<0)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目排序超过范围");
        }

    }

    /**
     * 点赞
     *
     * @param examQuestionAddRequest
     * @param loginUser
     * @return 提交记录id
     */
    @Override
    public long addExamQuestion(ExamQuestionAddRequest examQuestionAddRequest, User loginUser) {

        // 判断实体是否存在，根据类别获取实体
        Long examId = examQuestionAddRequest.getExamId();
        Long questionId = examQuestionAddRequest.getQuestionId();
        Exam exam = examService.getById(examId);
        if(exam == null) {
            throw new BusinessException((ErrorCode.NOT_FOUND_ERROR));
        }
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(exam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }


        ExamQuestion examQuestion = new ExamQuestion();
        BeanUtils.copyProperties(examQuestionAddRequest, examQuestion);
        //检查题目是否被添加
        boolean exists = lambdaQuery()
                .eq(ExamQuestion::getQuestionId , questionId)
                .eq(ExamQuestion::getExamId , examId)
                .exists();
        if(exists) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已添加");
        }

        boolean save = this.save(examQuestion);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "考试题目添加失败");
        }

        return examQuestion.getId();

    }


    /**
     * 查询考试题目目录
     *
     * @param examQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ExamQuestion> getQueryWrapper(ExamQuestionQueryRequest examQuestionQueryRequest) {
        QueryWrapper<ExamQuestion> queryWrapper = new QueryWrapper<>();
        if (examQuestionQueryRequest == null) {
            return queryWrapper;
        }


        Long examId = examQuestionQueryRequest.getExamId();
        Long questionId = examQuestionQueryRequest.getQuestionId();
        Long userId = examQuestionQueryRequest.getUserId();
        String sortField = examQuestionQueryRequest.getSortField();
        String sortOrder = examQuestionQueryRequest.getSortOrder();



        //拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(examId), "examId", examId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        //
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;

    }




    /**
     * 获取提交封装
     */
    @Override
    public ExamQuestionVO getExamQuestionVO(ExamQuestion examQuestion, User loginUser) {
        ExamQuestionVO examQuestionVO = ExamQuestionVO.objToVo(examQuestion);
        
        return examQuestionVO;
    }


    /**
     * 分页获取题目封装
     *
     * @param examQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<ExamQuestionVO> getExamQuestionVOPage(Page<ExamQuestion> examQuestionPage, HttpServletRequest request) {
        List<ExamQuestion> examQuestionList = examQuestionPage.getRecords();
        Page<ExamQuestionVO> examQuestionVOPage = new Page<>(examQuestionPage.getCurrent(), examQuestionPage.getSize(), examQuestionPage.getTotal());
        if (CollectionUtil.isEmpty(examQuestionList)) {
            return examQuestionVOPage;
        }

        /*
        * 先将所有的考试id放到列表中，再根据多条id查考试表，根据ide进行分组，得到每个id对应的用户信息
        * */
        Set<Long> examIdSet = examQuestionList.stream().map(ExamQuestion::getExamId).collect(Collectors.toSet());
        Map<Long, List<Exam>> examIdExamListMap = examService.listByIds(examIdSet).stream().collect(Collectors.groupingBy(Exam::getId));

        List<ExamQuestionVO> examQuestionVOList = examQuestionList.stream().map(examQuestion -> {
            ExamQuestionVO examQuestionVO = ExamQuestionVO.objToVo(examQuestion);
            Long examId = examQuestion.getExamId();
            Exam exam = null;
            if(examIdExamListMap.containsKey(examId)){
                exam = examIdExamListMap.get(examId).get(0);
            }
            examQuestionVO.setExamVO(examService.getExamVO(exam));
            examQuestionVO.setQuestionVO(questionService.getQuestionVO(questionService.getById(examQuestion.getQuestionId()),request));
            return examQuestionVO;
        }).collect(Collectors.toList());

        examQuestionVOPage.setRecords(examQuestionVOList);
        return examQuestionVOPage;
    }


    public Page<ExamQuestionVO> getMyExamQuestionVOPage(Page<ExamQuestion> examQuestionPage, HttpServletRequest request) {
        List<ExamQuestion> examQuestionList = examQuestionPage.getRecords();
        Page<ExamQuestionVO> examQuestionVOPage = new Page<>(examQuestionPage.getCurrent(), examQuestionPage.getSize(), examQuestionPage.getTotal());
        if (CollectionUtil.isEmpty(examQuestionList)) {
            return examQuestionVOPage;
        }

        User loginUser = userService.getLoginUser(request);
        /*
         * 先将所有的考试id放到列表中，再根据多条id查考试表，根据ide进行分组，得到每个id对应的用户信息
         * */
        Set<Long> examIdSet = examQuestionList.stream().map(ExamQuestion::getExamId).collect(Collectors.toSet());
        Map<Long, List<Exam>> examIdExamListMap = examService.listByIds(examIdSet).stream().collect(Collectors.groupingBy(Exam::getId));


        List<ExamQuestionVO> examQuestionVOList = examQuestionList.stream().map(examQuestion -> {
            ExamQuestionVO examQuestionVO = ExamQuestionVO.objToVo(examQuestion);
            Long examId = examQuestion.getExamId();
            Exam exam = null;
            if(examIdExamListMap.containsKey(examId)){
                exam = examIdExamListMap.get(examId).get(0);
            }
            examQuestionVO.setExamVO(examService.getExamVO(exam));
            examQuestionVO.setQuestionVO(questionService.getQuestionVO(questionService.getById(examQuestion.getQuestionId()),request));
            return examQuestionVO;
        }).collect(Collectors.toList());

        examQuestionVOPage.setRecords(examQuestionVOList);
        return examQuestionVOPage;
    }


    @Override
    public List<Long> getExamQuestionQuestionIdList(ExamQuestionQueryRequest examQuestionQueryRequest){
        QueryWrapper<ExamQuestion> queryWrapper = getQueryWrapper(examQuestionQueryRequest);
        List<ExamQuestion> examQuestionList = list(queryWrapper);
        List<Long> examQuestionQuestionIdList = examQuestionList.stream()
                .map(examQuestion -> examQuestion.getQuestionId())
                .collect(Collectors.toList());
        return examQuestionQuestionIdList;
    }

}






