package com.github.dadiyang.httpinvoker.requestor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.dadiyang.httpinvoker.annotation.ExpectedCode;
import com.github.dadiyang.httpinvoker.annotation.HttpReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 注册响应处理器，用于对后台返回的结果都是类似 {code: 0, msg/message: 'success', data: 'OK'} 的结构，
 * <p>
 * 此时我们只需要判断 code 是否为期望的值 (ExpectedCode中设置)，是的话，解析 data 的值否则抛出异常
 *
 * @author huangxuyang
 * @since  1.1.4
 */
@Component
public class ResultBeanResponseProcessor implements ResponseProcessor {
    private static final Logger log = LoggerFactory.getLogger(ResultBeanResponseProcessor.class);
    private static final String CODE = "code";
    private static final String DATA = "data";
    private static final String MESSAGE = "message";
    private static final String MSG = "msg";

    @Override
    public Object process(HttpResponse response, Method method) {
        // 对返回值进行解析，code 为 0，则返回反序列化 data 的值，否则抛出异常
        String rs = response.getBody();
        if (rs == null || rs.trim().isEmpty()) {
            return null;
        }
        JSONObject obj = JSON.parseObject(rs);
        if (isNotResultBean(obj)) {
            // 非 ResultBean 则解析整个返回结果
            return parseObject(method, rs);
        }
        //  标准的 ResultBean 包装类处理，进行解包处理，即只取 data 的值
        int expectedCode = getExpectedCode(method);
        if (obj.getIntValue(CODE) == expectedCode) {
            // code 为期望的值时说明返回结果是正确的
            return parseObject(method, obj.getString(DATA));
        } else {
            // 否则为接口返回错误
            HttpReq req = method.getAnnotation(HttpReq.class);
            String uri = req != null ? req.value() : method.getName();
            // 兼容两种错误信息的写法
            String errMsg = obj.containsKey(MESSAGE) ? obj.getString(MESSAGE) : obj.getString(MSG);
            String msg = "请求api失败, uri: " + uri + ", 错误信息: " + errMsg;
            log.warn(msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * 没有包含 code、msg/message 和 data 则不是 ResultBean
     */
    private boolean isNotResultBean(JSONObject obj) {
        return !obj.containsKey(CODE) || (!obj.containsKey(MSG) && !obj.containsKey(MESSAGE) && !obj.containsKey(DATA));
    }

    /**
     * 支持泛型的反序列化方法
     */
    private Object parseObject(Method method, String dataString) {
        if (dataString == null || dataString.trim().isEmpty()) {
            return null;
        }
        return JSON.parseObject(dataString, method.getGenericReturnType());
    }

    /**
     * 获取正确的 code 值，默认为0
     *
     * @return 正确的 code 值
     */
    private int getExpectedCode(Method method) {
        // 方法是有打注解，则使用方法上的
        if (method.isAnnotationPresent(ExpectedCode.class)) {
            ExpectedCode expectedCode = method.getAnnotation(ExpectedCode.class);
            return expectedCode.value();
        }
        // 否则使用类上的注解
        Class<?> clazz = method.getDeclaringClass();
        if (clazz.isAnnotationPresent(ExpectedCode.class)) {
            ExpectedCode expectedCode = clazz.getAnnotation(ExpectedCode.class);
            return expectedCode.value();
        }
        // 默认为 0
        return 0;
    }

}
