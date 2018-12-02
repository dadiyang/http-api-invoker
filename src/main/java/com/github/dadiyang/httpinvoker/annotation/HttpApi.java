package com.github.dadiyang.httpinvoker.annotation;

import com.github.dadiyang.httpinvoker.spring.HttpApiConfigurer;

import java.lang.annotation.*;

/**
 * Indicate the interface is a http service interface.
 * <p>
 * Those interfaces annotated by this annotation will be scanned by {@link HttpApiConfigurer}
 * and then register to the Spring container,
 * so that users can autowire the interfaces to their bean which depend on that interface's implementation.
 * <p>
 * To simplified the urls, use {@link #prefix} attribute to append prefix to the url in @HttpReq. ie. http://localhost:8080
 *
 * @author huangxuyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface HttpApi {
    /**
     * @return the prefix
     */
    String prefix() default "";
}
