package com.github.dadiyang.httpinvoker.annotation;

import java.lang.annotation.*;

/**
 * 表明一个接口的返回值不是 ResultBean，不需要 ResultBeanResponseProcessor 进行处理
 *
 * @author dadiyang
 * @since 2019-09-02
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface NotResultBean {
}
