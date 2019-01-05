package com.github.dadiyang.httpinvoker.annotation;

import java.lang.annotation.*;

/**
 * The {@link #value} stand for the key of request param and the annotated parameter represents the according value
 * <p>
 * value and isBody should not both be empty/false, otherwise the param will be ignored
 *
 * @author huangxuyang
 * date 2018/10/31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface Param {
    /**
     * value and isBody should not both be empty/false
     *
     * @return key of request param
     */
    String value() default "";

    /**
     * mark that the argument is the request body
     * <p>
     * if the argument is an non-primary object, all the field-value will be a part of request params.
     *
     * @return if the argument is the request body
     */
    boolean isBody() default false;
}
