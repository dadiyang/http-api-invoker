package com.github.dadiyang.httpinvoker.annotation;

import java.lang.annotation.*;

/**
 * The {@link #value} stand for the key of request param and the annotated parameter represents the according value
 *
 * @author huangxuyang
 * date 2018/10/31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface Param {
    /**
     * @return key of request param
     */
    String value();
}
