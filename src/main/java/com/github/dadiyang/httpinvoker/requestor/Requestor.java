package com.github.dadiyang.httpinvoker.requestor;

import java.io.IOException;

/**
 * 发送请求的工具
 *
 * @author huangxuyang
 * date 2018/11/1
 */
public interface Requestor {
    /**
     * 发送请求
     *
     * @param request the request info
     * @return 发送请求后的返回值
     * @throws IOException IO异常
     */
    HttpResponse sendRequest(HttpRequest request) throws IOException;
}
