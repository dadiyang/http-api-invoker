package com.github.dadiyang.httpinvoker.requestor;

import java.lang.reflect.Method;

/**
 * process response after got response.
 * <p>
 * we will be able to handle result before method return.
 *
 * @author dadiyang
 * date 2019/2/21
 */
public interface ResponseProcessor {

    /**
     * processing response before method return
     *
     * @param response response
     * @param method   the proxied method
     * @return the proxied method's return value
     */
    Object process(HttpResponse response, Method method);
}
