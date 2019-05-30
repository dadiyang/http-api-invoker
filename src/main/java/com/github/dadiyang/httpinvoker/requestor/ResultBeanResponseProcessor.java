package com.github.dadiyang.httpinvoker.requestor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.dadiyang.httpinvoker.annotation.ExpectedCode;
import com.github.dadiyang.httpinvoker.annotation.HttpReq;
import com.github.dadiyang.httpinvoker.util.ParamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册响应处理器，用于对后台返回的结果都是类似 {code: 0, msg/message: 'success', data: 'OK'} 的结构，
 * <p>
 * 此时我们只需要判断 code 是否为期望的值 (ExpectedCode中设置)，是的话，解析 data 的值否则抛出异常
 *
 * @author huangxuyang
 * @since 1.1.4
 */
@Component
public class ResultBeanResponseProcessor implements ResponseProcessor {
    private static final Logger log = LoggerFactory.getLogger(ResultBeanResponseProcessor.class);
    private static final String CODE = "code";
    private static final String DATA = "data";
    private static final String MESSAGE = "message";
    private static final String MSG = "msg";
    private Map<Class<?>, Boolean> isResultBeanCache = new ConcurrentHashMap<>();

    @Override
    public Object process(HttpResponse response, Method method) {
        // 对返回值进行解析，code 为 0，则返回反序列化 data 的值，否则抛出异常
        String rs = response.getBody();
        // 以下几种情况下，无需解析响应
        if (rs == null || rs.trim().isEmpty()) {
            return null;
        }
        Class<?> returnType = method.getReturnType();
        if (returnType == byte[].class) {
            return response.getBodyAsBytes();
        }
        if (returnType != Object.class && returnType.isAssignableFrom(BufferedInputStream.class)) {
            return response.getBodyStream();
        }
        if (returnType != Object.class && returnType.isAssignableFrom(response.getClass())) {
            return response;
        }
        ExpectedCode expectedCode = getExpectedAnnotation(method);
        // 如果返回值要求的就是一个 ResultBean，则不做处理
        if (Objects.equals(isResultBean(expectedCode, returnType), true)) {
            return parseObject(method, rs);
        }
        JSONObject obj = JSON.parseObject(rs);
        if (isResponseNotResultBean(expectedCode, obj)) {
            // 非 ResultBean 则解析整个返回结果
            return parseObject(method, rs);
        }

        //  标准的 ResultBean 包装类处理，进行解包处理，即只取 data 的值
        if (isExpectedCode(expectedCode, obj)) {
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

    private boolean isExpectedCode(ExpectedCode expectedCode, JSONObject obj) {
        if (expectedCode != null) {
            return isExpectedCode(obj, expectedCode.value(), expectedCode.codeFieldName(), expectedCode.ignoreFieldInitialCase());
        }
        // 默认 期望 0, 字段 code, 忽略首字母大小写
        return isExpectedCode(obj, 0, CODE, true);
    }


    private boolean isExpectedCode(JSONObject obj, int expectedCode, String codeField, boolean ignoreFieldInitialCase) {
        // 如果没有，而且需要忽略首字母大小写，则改变首字母大小写
        if (!obj.containsKey(codeField) && ignoreFieldInitialCase) {
            codeField = ParamUtils.changeInitialCase(codeField);
        }
        return Objects.equals(expectedCode, obj.getInteger(codeField));
    }

    private ExpectedCode getExpectedAnnotation(Method method) {
        // 方法是有打注解，则使用方法上的
        if (method.isAnnotationPresent(ExpectedCode.class)) {
            return method.getAnnotation(ExpectedCode.class);
        }
        // 否则使用类上的注解
        Class<?> clazz = method.getDeclaringClass();
        if (clazz.isAnnotationPresent(ExpectedCode.class)) {
            return clazz.getAnnotation(ExpectedCode.class);
        }
        return null;
    }

    /**
     * 判断一个 Class 是否为 ResultBean，即是否同时包含 code/msg/data 三个字段
     */
    private Boolean isResultBean(final ExpectedCode expectedCode, final Class<?> returnType) {
        return isResultBeanCache.computeIfAbsent(returnType, (type) -> {
            if (ParamUtils.isBasicType(returnType) || type.isInterface()) {
                return false;
            }
            Field[] fields = getDeclaredFields(type);
            boolean hasCode = false;
            boolean hasMsg = false;
            boolean hasData = false;
            String codeField = expectedCode == null ? CODE : expectedCode.codeFieldName();
            boolean ignoreInitialCase = expectedCode == null || expectedCode.ignoreFieldInitialCase();
            for (Field field : fields) {
                String fieldName = field.getName().toLowerCase();
                hasCode = hasCode || Objects.equals(codeField, fieldName) || (ignoreInitialCase && Objects.equals(fieldName, ParamUtils.changeInitialCase(codeField)));
                hasMsg = hasMsg || Objects.equals(MSG, fieldName) || Objects.equals(MESSAGE, fieldName);
                hasData = hasData || Objects.equals(DATA, fieldName);
            }
            return hasCode && hasMsg && hasData;
        });
    }

    /**
     * 获取字段，包含父级
     */
    private Field[] getDeclaredFields(Class<?> returnType) {
        if (returnType.getSuperclass() == Object.class) {
            return returnType.getDeclaredFields();
        } else {
            List<Field> fields = new ArrayList<>();
            Class<?> type = returnType;
            while (type != Object.class && !type.isInterface()) {
                fields.addAll(Arrays.asList(type.getDeclaredFields()));
                type = returnType.getSuperclass();
            }
            return fields.toArray(new Field[]{});
        }
    }

    /**
     * 没有包含 code、msg/message 和 data 则不是 ResultBean
     */
    private boolean isResponseNotResultBean(ExpectedCode expectedCode, JSONObject obj) {
        boolean hasCode = false;
        boolean hasMsg = false;
        boolean hasData = false;
        String codeField = expectedCode == null ? CODE : expectedCode.codeFieldName();
        boolean ignoreInitialCase = expectedCode == null || expectedCode.ignoreFieldInitialCase();
        for (String key : obj.keySet()) {
            String fieldName = key == null ? "" : key.toLowerCase();
            hasCode = hasCode || Objects.equals(codeField, fieldName) || (ignoreInitialCase && Objects.equals(fieldName, ParamUtils.changeInitialCase(codeField)));
            hasMsg = hasMsg || Objects.equals(MSG, fieldName) || Objects.equals(MESSAGE, fieldName);
            hasData = hasData || Objects.equals(DATA, fieldName);
        }
        return !hasCode || (!hasMsg && !hasData);
    }

    /**
     * 支持泛型的反序列化方法
     */
    private Object parseObject(Method method, String dataString) {
        if (dataString == null || dataString.trim().isEmpty()) {
            return null;
        }
        // 方法无需返回值
        Class<?> returnType = method.getReturnType();
        if (returnType == Void.class || returnType == void.class) {
            return null;
        } else if (returnType == Object.class
                || returnType == String.class
                || method.getReturnType() == CharSequence.class) {
            return dataString;
        }
        return JSON.parseObject(dataString, method.getGenericReturnType());
    }
}
