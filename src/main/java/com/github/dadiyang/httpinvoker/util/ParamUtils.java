package com.github.dadiyang.httpinvoker.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * utils for handling param
 *
 * @author dadiyang
 * @since 1.1.2
 */
public class ParamUtils {
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
        Map<String, String> map = new HashMap<>(obj.size(), 1);
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
                    value = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
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
}
