package com.github.dadiyang.httpinvoker;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.annotation.HttpApi;
import com.github.dadiyang.httpinvoker.annotation.HttpReq;
import com.github.dadiyang.httpinvoker.annotation.Param;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * an InvocationHandler which request the url that the annotation's value attribute specify when the annotated method is invoked.
 * <p>
 * And then parse the response body to the return value(if it has) using FastJson, generic type is also supported.
 *
 * @author huangxuyang
 * date 2018/11/27
 */
public class HttpApiInvoker implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpApiInvoker.class);
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^/]+?)}");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^/]+?)}");
    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^[a-zA-Z].+://");
    private static final String DEFAULT_PATH_SEPARATOR = "/";
    private Requestor requestor;
    private Properties properties;
    private Class<?> clazz;

    public HttpApiInvoker(Requestor requestor, Properties properties, Class<?> clazz) {
        this.requestor = requestor;
        this.properties = properties;
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!method.isAnnotationPresent(HttpReq.class)) {
            // those Object method invoke 'this' method
            if (this.getClass().getMethod(method.getName(), method.getParameterTypes()) != null) {
                return method.invoke(this, args);
            }
            // this proxy only implement those HttpReq-annotated method
            throw new IllegalStateException("this proxy only implement those HttpReq-annotated method");
        }
        String url = "";
        HttpReq anno = method.getAnnotation(HttpReq.class);
        // if the interface was annotated by @HttpApi and the url has no protocol
        if (clazz.isAnnotationPresent(HttpApi.class)
                && !PROTOCOL_PATTERN.matcher(anno.value()).find()) {
            HttpApi httpApi = clazz.getAnnotation(HttpApi.class);
            url += httpApi.prefix();
        }
        url += anno.value();
        // fill all config variables
        url = fillConfigVariables(url);
        // prepare param
        Map<String, Object> params = null;
        if (args != null && args.length > 0) {
            Map<String, Object> annotatedParam = parseAnnotatedParams(args, method);
            // use annotated param if exists
            if (!annotatedParam.isEmpty()) {
                params = annotatedParam;
            } else {
                // else use the first arg as param
                if (args[0] instanceof Collection || args[0] instanceof Array) {
                    // we don't handle collection param here
                    params = null;
                } else {
                    params = JSON.parseObject(JSON.toJSONString(args[0]));
                }
            }
            // fill path variable for the url
            url = fillPathVariables(params, url);
        }
        String response = requestor.sendRequest(url, params, args, anno);
        if (Objects.equals(method.getReturnType(), Void.class)) {
            return null;
        }
        if (response == null) {
            return null;
        } else {
            // get generic return type
            Type type = method.getGenericReturnType();
            return JSON.parseObject(response, type == null ? method.getReturnType() : type);
        }
    }


    /**
     * parse arguments annotated by @Param annotation to a map
     * <p>
     * the annotation's value stand for key and the argument represent value
     * <p>
     *
     * @param args   the arguments
     * @param method the method invoked
     * @return the map represent the params
     */
    private Map<String, Object> parseAnnotatedParams(Object[] args, Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        if (annotations.length <= 0) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = null;
        for (int i = 0, annotationsLength = annotations.length; i < annotationsLength; i++) {
            Annotation[] annotation = annotations[i];
            for (Annotation ann : annotation) {
                if (ann instanceof Param) {
                    Param param = (Param) ann;
                    String key = param.value();
                    if (map == null) {
                        map = new HashMap<>();
                    }
                    map.put(key, args[i]);
                }
            }
        }
        return map == null ? Collections.emptyMap() : map;
    }

    /**
     * replace the path variable for the specific param, and remove that param from the map
     *
     * @return the path variable filled url
     * @throws IllegalArgumentException thrown when the specific param absent
     */
    private String fillPathVariables(Map<String, Object> params, String url) {
        String[] path = url.split(DEFAULT_PATH_SEPARATOR);
        for (int i = 0; i < path.length; i++) {
            String s = path[i];
            Matcher matcher = PATH_VARIABLE_PATTERN.matcher(s);
            if (matcher.find()) {
                String key = matcher.group(1);
                if (params == null || !params.containsKey(key)) {
                    // path variable must be provided
                    String msg = "the url [" + url + "] needs a path variable: [" + key + "], but wasn't provided.";
                    log.warn(msg);
                    throw new IllegalArgumentException(msg);
                }
                path[i] = params.remove(key).toString();
            }
        }
        return String.join(DEFAULT_PATH_SEPARATOR, path);
    }

    private String fillConfigVariables(String url) {
        Matcher matcher = VARIABLE_PATTERN.matcher(url);
        if (matcher.find()) {
            String key = matcher.group(1);
            if (!properties.containsKey(key)) {
                // path variable must be provided
                String msg = "the url [" + url + "] needs a variable: [" + key + "], but wasn't provided.";
                log.warn(msg);
                throw new IllegalArgumentException(msg);
            }
            String prop = properties.getProperty(key);
            url = url.replace("${" + key + "}", prop);
        }
        return url;
    }
}
