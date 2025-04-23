package com.fyy.delyoj.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.fyy.delyoj.common.ErrorCode;
import com.fyy.delyoj.exception.BusinessException;
import com.fyy.delyoj.judge.codesandbox.CodeSandbox;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;

/*
* 远程代码沙箱实现
* */
public class RemoteCodeSandbox implements CodeSandbox {
    private final static String AUT_REQUEST_HEADER = "Authorization";
    private final static String AUT_REQUEST_SECRET = "Bearer123456789";
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码");
        String url = "http://62.234.18.18:8081/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUT_REQUEST_HEADER,AUT_REQUEST_SECRET)
                .body(json)
                .execute()
                .body();
        if(StringUtils.isBlank(responseStr)){
            throw new BusinessException(ErrorCode.API_REQUST_ERROR , "executeCode remoteSandbox error   message:" + responseStr);
        }
        return JSONUtil.toBean( responseStr , ExecuteCodeResponse.class);
    }
}
