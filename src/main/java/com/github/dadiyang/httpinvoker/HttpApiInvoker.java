package com.github.dadiyang.httpinvoker;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.annotation.*;
import com.github.dadiyang.httpinvoker.requestor.HttpRequest;
import com.github.dadiyang.httpinvoker.requestor.HttpResponse;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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
    private static final int OK_CODE_L = 200;
    private static final int OK_CODE_H = 300;
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
        url += anno.value();
        // fill config variables
        url = fillConfigVariables(url);

        // if the interface was annotated by @HttpApi and the url has no protocol
        if (clazz.isAnnotationPresent(HttpApi.class)
                && !PROTOCOL_PATTERN.matcher(url).find()) {
            HttpApi httpApi = clazz.getAnnotation(HttpApi.class);
            url = httpApi.prefix() + url;
        }

        // fill config variables again
        url = fillConfigVariables(url);

        // prepare param
        Map<String, Object> params = null;
        HttpRequest request = new HttpRequest(anno.timeout(), anno.method());
        if (args != null && args.length > 0) {
            Map<String, Object> annotatedParam = parseAnnotatedParams(args, method, request);
            // use annotated param if exists
            if (annotatedParam != null && !annotatedParam.isEmpty()) {
                params = annotatedParam;
            } else if (request.getBody() == null) {
                // else use the first arg as param
                request.setBody(args[0]);
            } else {
                request.setData(parseParam(request.getBody()));
            }
            // fill path variable for the url
            url = fillPathVariables(params, url);
        }
        request.setUrl(url);
        request.setData(params);
        HttpResponse response = requestor.sendRequest(request);
        if (response == null) {
            return null;
        }
        if (response.getStatusCode() < OK_CODE_L || response.getStatusCode() >= OK_CODE_H) {
            // status code is not 2xx
            throw new IOException(url + ": " + response.getStatusMessage());
        }
        if (Objects.equals(method.getReturnType(), Void.class)) {
            // without return type
            return null;
        }
        if (method.getReturnType() == String.class) {
            return response.getBody();
        }
        if (method.getReturnType() == byte[].class) {
            return response.getBodyAsBytes();
        }
        if (method.getReturnType().isAssignableFrom(BufferedInputStream.class)) {
            return response.getBodyStream();
        }
        if (method.getReturnType().isAssignableFrom(HttpResponse.class)) {
            return response;
        }
        // get generic return type
        Type type = method.getGenericReturnType();
        type = type == null ? method.getReturnType() : type;
        return JSON.parseObject(response.getBodyAsBytes(), type);

    }

    private Map<String, Object> parseParam(Object arg) {
        Map<String, Object> params;
        if (arg instanceof Collection
                || arg instanceof Array
                || arg.getClass().isArray()) {
            // we don't handle collection param here
            params = null;
        } else {
            params = JSON.parseObject(JSON.toJSONString(arg));
        }
        return params;
    }

    /**
     * parse arguments annotated by @Param annotation to a map
     * <p>
     * the annotation's value stand for key and the argument represent value
     * <p>
     *
     * @param args    the arguments
     * @param method  the method invoked
     * @param request
     * @return the map represent the params
     */
    private Map<String, Object> parseAnnotatedParams(Object[] args, Method method, HttpRequest request) {
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
                    if (map == null) {
                        map = new HashMap<>();
                    }
                    if (param.isBody()) {
                        request.setBody(args[i]);
                        request.setFileFormKey(param.value());
                    } else {
                        String key = param.value();
                        if (!key.isEmpty()) {
                            map.put(key, args[i]);
                        }
                    }
                    // ignore when the param annotation's value is empty and isBody is false
                }
                if (ann instanceof Headers) {
                    isMapStringString(method.getGenericParameterTypes()[i]);
                    request.setHeaders((Map<String, String>) args[i]);
                }
                if (ann instanceof Cookies) {
                    isMapStringString(method.getGenericParameterTypes()[i]);
                    request.setCookies((Map<String, String>) args[i]);
                }
            }
        }
        return map;
    }

    private void isMapStringString(Type arg) {
        if (!(arg instanceof ParameterizedType) || ((ParameterizedType) arg).getRawType() != Map.class) {
            throw new IllegalArgumentException("Headers annotation should only be annotated on parameter of Map<String, String&> type.");
        }
        Type[] types = ((ParameterizedType) arg).getActualTypeArguments();
        if (types[0] != String.class || types[1] != String.class) {
            throw new IllegalArgumentException("Headers annotation should only be annotated on parameter of Map<String, String&> type.");
        }
    }

    /**
     * replace the path variable for the specific param, and remove that param from the map
     *
     * @return the path variable filled url
     * @throws IllegalArgumentException thrown when the specific param absent
     */
    private String fillPathVariables(Map<String, Object> params, String url) {
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(url);
        while (matcher.find()) {
            String key = matcher.group(1);
            if (params == null
                    || !params.containsKey(key)
                    || params.get(key) == null) {
                // path variable must be provided
                String msg = "the url [" + url + "] needs a variable: [" + key + "], but wasn't provided.";
                log.warn(msg);
                throw new IllegalArgumentException(msg);
            }
            String prop = params.remove(key).toString();
            url = url.replace("{" + key + "}", prop);
        }
        return url;
    }

    private String fillConfigVariables(String url) {
        Matcher matcher = VARIABLE_PATTERN.matcher(url);
        while (matcher.find()) {
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
