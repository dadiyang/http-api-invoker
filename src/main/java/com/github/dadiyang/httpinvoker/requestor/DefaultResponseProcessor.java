package com.github.dadiyang.httpinvoker.requestor;

import com.github.dadiyang.httpinvoker.serializer.JsonSerializerDecider;
import com.github.dadiyang.httpinvoker.util.ObjectUtils;

import java.io.BufferedInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author huangxuyang
 * date 2019/2/21
 */
public class DefaultResponseProcessor implements ResponseProcessor {

    @Override
    public Object process(HttpResponse response, Method method) {
        // not need a return value
        if (ObjectUtils.equals(method.getReturnType(), Void.class)
                || ObjectUtils.equals(method.getReturnType(), void.class)) {
            return null;
        }
        String body = response.getBody();
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        // return body if return type is Object
        if (method.getReturnType() == Object.class) {
            return response.getBody();
        }
        if (method.getReturnType() == String.class
                || method.getReturnType() == CharSequence.class) {
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
        return JsonSerializerDecider.getJsonSerializer().parseObject(response.getBody(), type);
    }
}
