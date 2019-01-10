package com.github.dadiyang.httpinvoker.annotation;

import com.github.dadiyang.httpinvoker.requestor.Status;

import java.io.IOException;
import java.lang.annotation.*;

import static com.github.dadiyang.httpinvoker.requestor.Status.NOT_FOUND;
import static com.github.dadiyang.httpinvoker.requestor.Status.REDIRECT;
import static com.github.dadiyang.httpinvoker.requestor.Status.SERVER_ERROR;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface RetryPolicy {
    /**
     * retry times.
     * <p>
     * Default to 3
     *
     * @return retry times
     */
    int times() default 3;

    /**
     * Default for IOException
     *
     * @return retry if the provided exception occur
     */
    Class<? extends Throwable>[] retryFor() default IOException.class;

    /**
     * Default for all not 20x code
     *
     * @return retry if the status codes gotten
     */
    Status[] retryForStatus() default {NOT_FOUND, REDIRECT, SERVER_ERROR};

    /**
     * fixed milli to sleep before retry
     * <p>
     * Default for 0
     *
     * @return fixed milli
     */
    long fixedBackOffPeriod() default 0;
}
