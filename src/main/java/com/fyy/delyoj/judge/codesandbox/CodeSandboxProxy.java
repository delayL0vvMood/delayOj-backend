package com.fyy.delyoj.judge.codesandbox;

import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;


/*
* proxy代理模式
*
* */
@Slf4j
public class CodeSandboxProxy implements CodeSandbox{

    /*
    * 引入代码沙箱类
    * */
    private CodeSandbox codeSandbox;

    public CodeSandboxProxy(CodeSandbox codeSandbox){
        this.codeSandbox = codeSandbox;

    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息:"+ executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        log.info("代码沙箱响应信息:"+ executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
