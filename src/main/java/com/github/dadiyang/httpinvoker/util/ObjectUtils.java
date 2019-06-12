package com.github.dadiyang.httpinvoker.util;

/**
 * @author dadiyang
 * @since 2019-06-12
 */
public class ObjectUtils {
    private ObjectUtils() {
        throw new UnsupportedOperationException("utils should not be initialized!");
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static void requireNonNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }
}
