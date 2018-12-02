package com.github.dadiyang.httpinvoker.annotation;

import java.lang.annotation.*;

/**
 * Indicate the interface is a http api interface.
 * <p>
 * To simplified the urls, use {@link #prefix} attribute to set the prefix of url in @HttpReq. ie. http://localhost:8080
 * <p>
 * Those interfaces annotated by this annotation will be scanned by {@link com.github.dadiyang.httpinvoker.spring.HttpApiConfigurer}
 * so that users can autowire the interface.
 * <p>
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
