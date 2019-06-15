package com.github.dadiyang.httpinvoker.annotation;

import java.lang.annotation.*;

/**
 * Indicates the parameter is a header map.
 *
 * @author huangxuyang
 * date 2018/12/21
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
public @interface Headers {
    String[] keys() default "";

    String[] values() default "";
}
