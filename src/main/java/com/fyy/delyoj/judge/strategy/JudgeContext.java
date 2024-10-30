package com.fyy.delyoj.judge.strategy;

import com.fyy.delyoj.model.dto.question.JudgeCase;
import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import com.fyy.delyoj.model.entity.Question;
import com.fyy.delyoj.model.entity.QuestionSubmit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private Question question;

    private QuestionSubmit questionSubmit;

    private List<JudgeCase> judgeCaseList;


}
