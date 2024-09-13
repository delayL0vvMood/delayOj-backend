package com.fyy.delyoj.model.dto.questionSubmit;
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
@EqualsAndHashCode(callSuper = true)
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private Integer status;

    /**
     * 题目 id
     */
    private Long questionId;

    private Long userId;



    private static final long serialVersionUID = 1L;
}