package com.fyy.delyoj.model.dto.examquestion;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子点赞请求
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@Data
public class ExamQuestionAddRequest implements Serializable {
    /**
     * 考试id
     */
    private Long examId;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 题目顺序（从1开始）
     */
    private Integer questionOrder;


    /**
     * 本题分数
     */
    private Integer score;



    private static final long serialVersionUID = 1L;
}