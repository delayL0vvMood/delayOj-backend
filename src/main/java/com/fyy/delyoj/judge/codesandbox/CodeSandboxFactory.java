package com.fyy.delyoj.judge.codesandbox;

import com.fyy.delyoj.judge.codesandbox.impl.RemoteCodeSandbox;
import com.fyy.delyoj.judge.codesandbox.impl.ThirdPartyCodeSandbox;
import com.fyy.delyoj.judge.codesandbox.impl.ExampleCodeSandbox;

public class CodeSandboxFactory {
    public static CodeSandbox newInstance(String type) {
        switch (type){
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            default:
                return new ExampleCodeSandbox();

        }

    }
}
