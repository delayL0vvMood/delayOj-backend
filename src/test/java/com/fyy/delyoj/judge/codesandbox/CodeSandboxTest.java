package com.fyy.delyoj.judge.codesandbox;

import com.fyy.delyoj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.fyy.delyoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.fyy.delyoj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CodeSandboxTest {

    @Value("${codesandbox.type:example}")
    private String type;


    /*
    * 通过工厂类
    *
    * */
    @Test
    void executeCodeByValue() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "public class HelloWorld ";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2 3 4 5" , "6 7 8 9 10");
        /*
         * 链式调用
         * */
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();

        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        /*
         *判断是否为空
         *  */
        Assertions.assertNotNull(executeCodeResponse);
    }


    /*
     *通过代理类创建
     *
     * */
    @Test
    void executeCodeByProxy() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String code = "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        int var1 = Integer.parseInt(args[0]);\n" +
                "        int var2 = Integer.parseInt(args[1]);\n" +
                "        int var3 = var1 + var2;\n" +
                "        System.out.println(\"结果是：\" + var3);\n" +
                "    }\n" +
                "\n" +
                "}\n";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2" , "6 7");
        /*
         * 链式调用
         * */
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();

        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        /*
         *判断是否为空
         *  */
        Assertions.assertNotNull(executeCodeResponse);
    }

}