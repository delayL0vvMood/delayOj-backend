package com.fyy.delyoj.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子视图
 *
 * @author <a href="https://github.com/lifyy">程序员鱼皮</a>
 * @from <a href="https://fyy.icu">编程导航知识星球</a>
 */
@Data
public class ExamVO implements Serializable {

    private Long id;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 考试描述
     */
    private String examDesc;

    /**
     * 创建者用户id
     */
    private Long userId;

    /**
     * 考试开始时间
     */
    private Date startTime;

    /**
     * 考试结束时间
     */
    private Date endTime;

    /**
     * 状态：0-未开始 1-进行中 2-已结束
     */
    private Integer status;

    /**
     * 是否公开：0-私有 1-公开
     */
    private Integer isPublic;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /*
    * 创建人题目信息
    *
    * */
    private UserVO userVO;



    private static final long serialVersionUID = 1L;
}
