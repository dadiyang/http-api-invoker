package com.github.dadiyang.httpinvoker.annotation;

import java.lang.annotation.*;

/**
 * indicate a request with Content-Type of application/x-www-form-urlencoded
 *
 * @author dadiyang
 * @since 1.1.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Form {
}
