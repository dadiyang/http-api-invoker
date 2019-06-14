package com.github.dadiyang.httpinvoker.util;

/**
 * @author dadiyang
 * @since 2019-06-13
 */
public class StringUtils {
    private StringUtils() {
        throw new UnsupportedOperationException("utils should not be initialized!");
    }

    public static String upperCase(String str) {
        return str == null ? null : str.toUpperCase();
    }
}
