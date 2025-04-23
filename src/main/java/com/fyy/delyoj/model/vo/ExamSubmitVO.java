package com.fyy.delyoj.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import com.fyy.delyoj.model.entity.ExamSubmit;
import com.fyy.delyoj.model.entity.QuestionSubmit;
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
public class ExamSubmitVO implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /*
    * 考试id
    * */
    private Long examId;
    
    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息（json 对象）
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
     */
    private Integer status;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 创建用户 id
     */
    private Long userId;

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

    private UserVO userVO;


    /*
    *
    * 对应题目信息
    *
    * */
    private  QuestionVO questionVO;
    
    /*
    * 
    * 考试封装类
    * */
    private  ExamVO examVO;


    /*
    * 封装类转换成实体类
    *
    * */
    public static QuestionSubmit voToObj(ExamSubmitVO examSubmitVO) {
        if (examSubmitVO == null) {
            return null;
        }
        QuestionSubmit examSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(examSubmitVO, examSubmit);
        JudgeInfo judgeInfoObj = examSubmitVO.getJudgeInfo();
        if(judgeInfoObj != null){
            examSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoObj));
        }
        return examSubmit;
    }
    

    /*
    * 实体类转换成封装类
    *
    * */
    
    public static ExamSubmitVO objToVo(ExamSubmit examSubmit) {
        if (examSubmit == null) {
            return null;
        }
        ExamSubmitVO examSubmitVO = new ExamSubmitVO();
        BeanUtils.copyProperties(examSubmit, examSubmitVO);
        String judgeInfoStr = examSubmit.getJudgeInfo();
        examSubmitVO.setJudgeInfo(JSONUtil.toBean(judgeInfoStr, JudgeInfo.class));
        return examSubmitVO;
    }

    private static final long serialVersionUID = 1L;
}
