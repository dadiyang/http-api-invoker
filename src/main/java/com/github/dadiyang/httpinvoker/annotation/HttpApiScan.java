package com.github.dadiyang.httpinvoker.annotation;

import com.github.dadiyang.httpinvoker.spring.HttpApiConfigurer;

import java.lang.annotation.*;

/**
 * Similar to @ComponentScan in Spring,
 * the {@link #value} specify the base packages the {@link HttpApiConfigurer } would scan.
 * <p>
 * {@link #configPaths} specify the config files paths
 * <p>
 * If specific packages are not defined, scanning will occur from the package of the class that declares this annotation.
 *
 * @author huangxuyang
 * @date 2018/11/1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface HttpApiScan {
    /**
     * base packages, which the http api interfaces contain
     *
     * @return base packages
     */
    String[] value() default "";

    /**
     * the config file path, if you use ${...} placeholder, this is needed.
     *
     * @return the config file path
     */
    String[] configPaths() default "";
}
