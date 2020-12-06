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

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
