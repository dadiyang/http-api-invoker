package com.github.dadiyang.httpinvoker.exception;

/**
 * Signals that a request has received an unexpected result.
 *
 * @author dadiyang
 * @since 2019-07-09
 */
public class UnexpectedResultException extends IllegalStateException {
    public UnexpectedResultException() {
    }

    public UnexpectedResultException(String s) {
        super(s);
    }

    public UnexpectedResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedResultException(Throwable cause) {
        super(cause);
    }
}
