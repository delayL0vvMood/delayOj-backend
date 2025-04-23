package com.fyy.delyoj.model.dto.examuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fyy.delyoj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExamUserQueryRequest extends PageRequest implements Serializable {

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




    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}