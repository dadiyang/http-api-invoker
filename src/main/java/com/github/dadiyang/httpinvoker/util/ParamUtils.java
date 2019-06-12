package com.github.dadiyang.httpinvoker.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.*;

/**
 * utils for handling param
 *
 * @author dadiyang
 * @since 1.1.2
 */
public class ParamUtils {
    private static final char UPPER_A = 'A';
    private static final char UPPER_Z = 'Z';
    private static final char LOWER_A = 'a';
    private static final char LOWER_Z = 'z';
    private static final List<Class<?>> BASIC_TYPE = Arrays.asList(Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Character.class,
            Boolean.class, String.class, Void.class);

    private ParamUtils() {
        throw new UnsupportedOperationException("utils should not be initialized!");
    }

    /**
     * check if the clz is primary type, primary type's wrapper, String or Void
     *
     * @param clz the type
     * @return check if the clz is basic type
     */
    public static boolean isBasicType(Class<?> clz) {
        if (clz == null) {
            return false;
        }
        return clz.isPrimitive() || BASIC_TYPE.contains(clz);
    }

    /**
     * check if the arg is a collection
     *
     * @param arg object to be checked
     * @return if the arg is a array/collection
     */
    public static boolean isCollection(Object arg) {
        if (arg == null) {
            return false;
        }
        return arg.getClass().isArray()
                || arg instanceof Collection
                || arg instanceof Array;
    }

    /**
     * convert an object to Map&lt;String, String&gt;
     *
     * @param arg object to be converted
     * @return Map&lt;String, String&gt; represent the arg
     */
    public static Map<String, String> toMapStringString(Object arg) {
        if (arg == null) {
            return Collections.emptyMap();
        }
        JSONObject obj = JSON.parseObject(JSON.toJSONString(arg));
        Map<String, String> map = new HashMap<String, String>(obj.size(), 1);
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            map.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue().toString());
        }
        return map;
    }

    /**
     * convert param object to query string
     * <p>
     * collection fields will be convert to a form of duplicated key such as id=1&amp;id=2&amp;id=3
     *
     * @param arg the param args
     * @return query string
     */
    public static String toQueryString(Object arg) {
        if (arg == null) {
            return "";
        }
        StringBuilder qs = new StringBuilder("?");
        JSONObject obj = JSON.parseObject(JSON.toJSONString(arg));
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (isCollection(entry.getValue())) {
                qs.append(collectionToQueryString(obj, entry));
            } else {
                String value = entry.getValue() == null ? "" : entry.getValue().toString();
                try {
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
                qs.append(entry.getKey()).append("=").append(value).append("&");
            }
        }
        return qs.substring(0, qs.length() - 1);
    }

    private static String collectionToQueryString(JSONObject obj, Map.Entry<String, Object> entry) {
        JSONArray arr = obj.getJSONArray(entry.getKey());
        StringBuilder valBuilder = new StringBuilder();
        for (Object item : arr) {
            valBuilder.append(entry.getKey()).append("=").append(item).append("&");
        }
        return valBuilder.toString();
    }

    public static char changeCase(char c) {
        if (c >= UPPER_A && c <= UPPER_Z) {
            return c += 32;
        } else if (c >= LOWER_A && c <= LOWER_Z) {
            return c -= 32;
        } else {
            return c;
        }
    }

    public static String changeInitialCase(String c) {
        if (c == null || c.isEmpty()) {
            return c;
        }
        return changeCase(c.charAt(0)) + c.substring(1);
    }
}
