package com.fyy.delyoj.judge;

import com.fyy.delyoj.judge.strategy.*;
import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import com.fyy.delyoj.model.entity.ExamSubmit;
import com.fyy.delyoj.model.entity.QuestionSubmit;
import com.fyy.delyoj.model.enums.QuestionSubmitLanguageEnum;

public class JudgeManager {

    /*
    * 判题策略管理类
    * 判断使用哪种策略进行判题
    *
    * */
    JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if(QuestionSubmitLanguageEnum.JAVA.getValue().equals(language)){
            judgeStrategy = new JavaJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }


}
