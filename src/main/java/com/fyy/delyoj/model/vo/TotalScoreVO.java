package com.fyy.delyoj.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子视图
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@Data
public class TotalScoreVO implements Serializable {

    /**
     * 考试id
     */
    private Long examId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 得分
     */
    private Double totalScore;


    /*
    *  考试题目情况
    * */
    private  String questionScores;



    private static final long serialVersionUID = 1L;
}
