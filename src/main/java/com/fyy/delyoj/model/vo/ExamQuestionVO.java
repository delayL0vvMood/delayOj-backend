package com.fyy.delyoj.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fyy.delyoj.model.entity.ExamQuestion;
import com.fyy.delyoj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目提交封装类
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@Data
public class ExamQuestionVO implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 考试id
     */
    private Long examId;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 本题分数
     */
    private Integer score;

    /**
     * 题目顺序（从1开始）
     */
    private Integer questionOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /*
    * 提交者信息
    *
    * */

    private long userId;

    /*
    *
    * 创建人信息
    * */
    private UserVO userVO;

    /*
    * 考试信息
    * */
    private  ExamVO examVO;

    /*
    * 题目信息
    * */
    private QuestionVO questionVO;

    /*
    * 封装类转换成实体类
    *
    * */
    public static ExamQuestion voToObj(ExamQuestionVO examQuestionVO) {
        if (examQuestionVO == null) {
            return null;
        }
        ExamQuestion examQuestion = new ExamQuestion();
        BeanUtils.copyProperties(examQuestionVO, examQuestion);
        return examQuestion;
    }
    

    /*
    * 实体类转换成封装类
    *
    * */
    
    public static ExamQuestionVO objToVo(ExamQuestion examQuestion) {
        if (examQuestion == null) {
            return null;
        }
        ExamQuestionVO examQuestionVO = new ExamQuestionVO();
        BeanUtils.copyProperties(examQuestion, examQuestionVO);
        return examQuestionVO;
    }

    private static final long serialVersionUID = 1L;
}
