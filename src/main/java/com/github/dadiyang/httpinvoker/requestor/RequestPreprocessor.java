package com.github.dadiyang.httpinvoker.requestor;

import java.lang.reflect.Method;

/**
 * pre-process request before send it.
 * <p>
 * we can modify headers, cookies, params or even url of the request before actually send it.
 *
 * @author dadiyang
 * date 2019/1/9
 */
public interface RequestPreprocessor {
    /**
     * for compatible, we use a ThreadLocal to store current method
     */
    ThreadLocal<Method> CURRENT_METHOD_THREAD_LOCAL = new ThreadLocal<Method>();

    /**
     * Pre-processing the request
     *
     * @param request the request to send
     */
    void process(HttpRequest request);
}
