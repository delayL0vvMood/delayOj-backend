package com.fyy.delyoj.judge.codesandbox;

import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeSandboxProxy implements CodeSandbox{
    private static final Logger log = LoggerFactory.getLogger(CodeSandboxProxy.class);

    private CodeSandbox codeSandbox;
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息:"+ executeCodeRequest.toString());

    }
}
