package com.fyy.delyoj.judge;

import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.fyy.delyoj.model.entity.QuestionSubmit;
import com.fyy.delyoj.service.QuestionSubmitService;

import javax.annotation.Resource;

public interface JudgeService {

    QuestionSubmit doJudge(Long QuestionSubmitId);
}
