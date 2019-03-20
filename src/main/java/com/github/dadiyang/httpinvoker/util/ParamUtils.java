package com.github.dadiyang.httpinvoker.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
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

}
