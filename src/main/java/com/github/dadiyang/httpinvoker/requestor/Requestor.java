package com.github.dadiyang.httpinvoker.requestor;

import com.github.dadiyang.httpinvoker.annotation.HttpReq;

import java.io.IOException;
import java.util.Map;

/**
 * 发送请求的工具
 *
 * @author huangxuyang
 * date 2018/11/1
 */
public interface Requestor {
    /**
     * 发送语法
     *
     * @param url    url
     * @param params 请求参数
     * @param args   方法参数
     * @param anno   HttpReq 注解
     * @return 发送请求后的返回值
     * @throws IOException IO异常
     */
    HttpResponse sendRequest(String url, Map<String, Object> params, Object[] args, HttpReq anno) throws IOException;
}
