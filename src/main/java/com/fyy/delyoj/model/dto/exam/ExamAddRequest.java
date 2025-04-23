package com.fyy.delyoj.model.dto.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fyy.delyoj.model.dto.question.JudgeCase;
import com.fyy.delyoj.model.dto.question.JudgeConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建请求
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@Data
public class ExamAddRequest implements Serializable {
    /**
     * 考试名称
     */
    private String examName;

    /**
     * 考试描述
     */
    private String examDesc;

    /**
     * 考试开始时间
     */
    private Date startTime;

    /**
     * 考试结束时间
     */
    private Date endTime;

    /**
     * 是否公开：0-私有 1-公开
     */
    private Integer isPublic;

    /**
     * 访问密码
     */
    private String examPassword;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}