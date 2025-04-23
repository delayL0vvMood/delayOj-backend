package com.fyy.delyoj.model.dto.userscore;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子点赞请求
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@Data
public class UserScoreAddRequest implements Serializable {

    /**
     * 考试id
     */
    private Long examId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 得分
     */
    private Integer score;

    /*
    *
    * */
    private String judgeResult;


    private static final long serialVersionUID = 1L;
}