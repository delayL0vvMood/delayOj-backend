package com.fyy.delyoj.judge.codesandbox.impl;

import com.fyy.delyoj.judge.codesandbox.CodeSandbox;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;


/*
* 示例代码沙箱
*
* */
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("示例代码");
        return null;
    }
}
