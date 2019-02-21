package com.github.dadiyang.httpinvoker.requestor;

import com.alibaba.fastjson.JSON;

import java.io.BufferedInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author huangxuyang
 * @date 2019/2/21
 */
public class DefaultResponseProcessor implements ResponseProcessor {
    @Override
    public Object process(HttpResponse response, Method method) {
        if (method.getReturnType() == String.class) {
            return response.getBody();
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
