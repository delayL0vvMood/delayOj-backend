package com.fyy.delyoj.model.dto.examuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新请求
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@Data
public class ExamUserUpdateRequest implements Serializable {

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
     * 用户id
     */
    private Long userId;

    /**
     * 用户进入考试时间
     */
    private Date joinTime;

    /**
     * 用户提交时间
     */
    private Date submitTime;

    /**
     * 状态：0-未开始 1-进行中 2-已提交 3-超时未提交
     */
    private Integer status;


    private static final long serialVersionUID = 1L;
}