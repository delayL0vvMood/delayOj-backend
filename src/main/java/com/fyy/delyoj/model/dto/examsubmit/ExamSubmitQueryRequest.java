package com.fyy.delyoj.model.dto.examsubmit;
import com.fyy.delyoj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子点赞请求
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@Data
/*
* 查询排序
* */
@EqualsAndHashCode(callSuper = true)
public class ExamSubmitQueryRequest extends PageRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private Integer status;

    /*
    * 考试id
    * */
    private Long examId;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 用户 id
     */

    private Long userId;



    private static final long serialVersionUID = 1L;
}