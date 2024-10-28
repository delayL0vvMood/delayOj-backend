package com.fyy.delyoj.judge.codesandbox;

import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {


    /*
    * 执行代码
    * @Param executeCodeRequest 代码执行请求
    *
    *
    * */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
