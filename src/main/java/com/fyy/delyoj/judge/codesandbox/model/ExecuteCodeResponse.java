package com.fyy.delyoj.judge.codesandbox.model;

import com.fyy.delyoj.model.dto.questionSubmit.JudgeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {
    /*
    * 结果
    * */
    private List<String> outputList;

    /*
    * 信息
    * */
    private String massage;

    /*
    * 执行状态
    * */
   private String status;

    /*
    * 判题信息
    * */
    private JudgeInfo judgeInfo;


}
