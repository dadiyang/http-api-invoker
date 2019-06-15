package com.github.dadiyang.httpinvoker.annotation;

import java.lang.annotation.*;

/**
 * @author dadiyang
 * @since 2019-06-15
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ContentType {
    String value() default "";
}
