package com.github.dadiyang.httpinvoker.requestor;

import com.alibaba.fastjson.JSON;

import java.io.BufferedInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author huangxuyang
 * date 2019/2/21
 */
public class DefaultResponseProcessor implements ResponseProcessor {
    @Override
    public Object process(HttpResponse response, Method method) {
        // not need a return value
        if (Objects.equals(method.getReturnType(), Void.class)
                || Objects.equals(method.getReturnType(), void.class)) {
            return null;
        }
        String body = response.getBody();
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        if (method.getReturnType() == String.class) {
            return body;
        }
        if (method.getReturnType() == byte[].class) {
            return response.getBodyAsBytes();
        }
        if (method.getReturnType().isAssignableFrom(BufferedInputStream.class)) {
            return response.getBodyStream();
        }
        if (method.getReturnType().isAssignableFrom(response.getClass())) {
            return response;
        }
        // get generic return type
        Type type = method.getGenericReturnType();
        type = type == null ? method.getReturnType() : type;
        return JSON.parseObject(response.getBodyAsBytes(), type);
    }
}
