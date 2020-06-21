package com.github.dadiyang.httpinvoker.annotation;

import java.lang.annotation.*;

/**
 * Indicates the http request related information.
 * <p>
 * The url is specified by {@link #value}, and the {@link #method} declares the request method such as GET/POST/PUT
 * and {@link #timeout} provides the request timeout.
 *
 * @author huangxuyang
 * date 2018/10/30
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpReq {

    /**
     * the service's url, path variable is supported
     *
     * @return the service's url
     */
    String value();

    /**
     * GET，POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE
     *
     * @return 请求方式
     */
    String method() default "GET";

    /**
     * request timeout in millisecond
     *
     * @return request timeout
     */
    int timeout() default 5000;

}
