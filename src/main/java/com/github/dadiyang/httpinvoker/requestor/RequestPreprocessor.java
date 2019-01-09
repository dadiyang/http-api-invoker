package com.github.dadiyang.httpinvoker.requestor;

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
     * Pre-processing the request
     *
     * @param request the request to send
     */
    void process(HttpRequest request);
}
