package com.github.dadiyang.httpinvoker.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射工具类
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class ReflectionUtils {
    private static Map<String, Boolean> existCache = new ConcurrentHashMap<String, Boolean>();

    private ReflectionUtils() {
        throw new UnsupportedOperationException("静态工具类不允许被实例化");
    }

    /**
     * 检查某个全类名是否存在于 classpath 中
     */
    public static boolean classExists(String clzFullName) {
        if (StringUtils.isBlank(clzFullName)) {
            return false;
        }
        Boolean rs = existCache.get(clzFullName);
        if (rs != null && rs) {
            return true;
        }
        try {
            Class<?> clz = Class.forName(clzFullName);
            existCache.put(clzFullName, clz != null);
            return clz != null;
        } catch (ClassNotFoundException e) {
            existCache.put(clzFullName, false);
            return false;
        }
    }
}
