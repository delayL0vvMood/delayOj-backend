package com.fyy.delyoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.constant.CommonConstant;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.exception.ThrowUtils;
import com.fyy.delyoj.model.dto.exam.ExamQueryRequest;
import com.fyy.delyoj.model.dto.examuser.ExamUserQueryRequest;
import com.fyy.delyoj.model.entity.Exam;
import com.fyy.delyoj.model.vo.ExamVO;
import com.fyy.delyoj.service.*;
import com.fyy.delyoj.mapper.ExamMapper;
import com.fyy.delyoj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author fengyaoyang
* @description 针对表【exam(考试表)】的数据库操作Service实现
* @createDate 2025-03-10 16:00:02
*/
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam>
    implements ExamService{
    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ExamRedisService examRedisService;

    @Resource
    private ThreadPoolTaskScheduler taskScheduler;

    @Override
    public boolean saveExam(Exam exam) {
        boolean save = save(exam);
        if (save) {
            scheduleStatusUpdate(exam);
        }
        return save;
    }

    /*
    * 重启任务恢复
    * */
    @PostConstruct
    public void init() {
        List<Exam> examList = list();
        System.out.println("重启任务恢复" + examList.size());
        examList.forEach(this::scheduleStatusUpdate);
    }

    private void scheduleStatusUpdate(Exam exam) {
        // 计算当前时间与考试开始/结束时间的延迟
        long startDelay = exam.getStartTime().getTime() - System.currentTimeMillis();
        long endDelay = exam.getEndTime().getTime() - System.currentTimeMillis();
        startDelay = Math.max(0, startDelay);
        endDelay = Math.max(0, endDelay);

        Exam startExam = new Exam();
        Exam endExam = new Exam();
        startExam.setId(exam.getId());
        startExam.setStatus(1);
        endExam.setId(exam.getId());
        endExam.setStatus(2);

        // 调度开始任务：status 0 → 1
        if (startDelay > 0) {
            taskScheduler.schedule(
                    () -> updateStatusWithCheck(startExam),
                    Instant.now().plusMillis(startDelay)
            );
        }

        // 调度结束任务：status 1 → 2
        if (endDelay > 0) {
            taskScheduler.schedule(
                    () -> updateStatusWithCheck(endExam),
                    Instant.now().plusMillis(endDelay)
            );
        }
    }
    @Transactional(rollbackFor = Exception.class) // 添加事务注解
    public boolean updateStatusWithCheck(Exam updateEntity) {
        // 示例：更新前校验当前状态
        Exam exam = getById(updateEntity.getId());
        if (exam.getStatus() + 1 != updateEntity.getStatus()) {
            throw new IllegalStateException("非法状态跃迁");
        }
        System.out.println("初始化考试排名");
        examRedisService.initExamRank(exam);
        return updateById(updateEntity);
    }

    @Override
    public void validExam(Exam exam, boolean add) {
        if (exam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = exam.getId();
        String examName = exam.getExamName();
        String examDesc = exam.getExamDesc();
        Date startTime = exam.getStartTime();
        Date endTime = exam.getEndTime();
        Integer isPublic = exam.getIsPublic();
        String examPassword = exam.getExamPassword();
        


        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(examName) || ObjectUtils.anyNull(isPublic,startTime,endTime), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(examName) && examName.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(examPassword) && examPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过长");
        }
        if (StringUtils.isNotBlank(examDesc) && examDesc.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "简介过长");
        }
        if (ObjectUtils.isNotEmpty(isPublic) && (isPublic >1&&isPublic<0)){
            throw  new BusinessException( ErrorCode.PARAMS_ERROR, "参数错误");
        }
    }

    /**
     * 获取查询包装类
     * 用户根据字段查询，根据前端传来的请求对象，得到mybatis框架支持的queryWrapper类
     *
     *
     * @param examQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Exam> getQueryWrapper(ExamQueryRequest examQueryRequest) {
        QueryWrapper<Exam> queryWrapper = new QueryWrapper<>();
        if (examQueryRequest == null) {
            return queryWrapper;
        }

        Long id = examQueryRequest.getId();
        String examName = examQueryRequest.getExamName();
        String examDesc = examQueryRequest.getExamDesc();
        Date startTime = examQueryRequest.getStartTime();
        Date endTime = examQueryRequest.getEndTime();
        Integer status = examQueryRequest.getStatus();
        Integer isPublic = examQueryRequest.getIsPublic();
        Long userId = examQueryRequest.getUserId();
        int current = examQueryRequest.getCurrent();
        int pageSize = examQueryRequest.getPageSize();
        String sortField = examQueryRequest.getSortField();
        String sortOrder = examQueryRequest.getSortOrder();





        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(examName), "examName", examName);
        queryWrapper.like(StringUtils.isNotBlank(examDesc), "examDesc", examDesc);
        queryWrapper.like(ObjectUtils.isNotEmpty(startTime), "startTime", startTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId",userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq("isDelete", false);
        queryWrapper.eq(ObjectUtils.isNotEmpty(isPublic),"isPublic",1);/// 0-私有，1-公开
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }

    @Override
    public Page<Exam> searchFromEs(ExamQueryRequest examQueryRequest) {
        return null;
    }


    /*
     *
     * 获取封装类
     * 查询创建人信息
     * */
    @Override
    public ExamVO getExamVO(Exam exam) {
        ExamVO examVO = new ExamVO();
        BeanUtils.copyProperties(exam, examVO);
        examVO.setUserVO(userService.getUserVO(userService.getById(exam.getUserId())));
        return examVO;
    }

    @Override
    public Page<ExamVO> getExamVOPage(Page<Exam> examPage, HttpServletRequest request) {
        List<Exam> examList = examPage.getRecords();
        Page<ExamVO> examVOPage = new Page<>(examPage.getCurrent(), examPage.getSize(), examPage.getTotal());
        if (CollectionUtils.isEmpty(examList)) {
            return examVOPage;
        }
        // 填充信息
        List<ExamVO> examVOList = examList.stream()
                .map(exam -> getExamVO(exam))
                .collect(Collectors.toList());
        examVOPage.setRecords(examVOList);
        return examVOPage;
    }

}




