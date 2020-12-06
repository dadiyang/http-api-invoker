package com.github.dadiyang.httpinvoker.serializer;

import com.github.dadiyang.httpinvoker.util.ReflectionUtils;

/**
 * @author dadiyang
 * @since 2020/12/5
 */
public class JsonSerializerDecider {
    private static final String FAST_JSON_CLASS = "com.alibaba.fastjson.JSON";
    private static final String GSON_CLASS = "com.google.gson.Gson";

    public static JsonSerializer getJsonSerializer() {
        // 默认使用 FAST_JSON
        if (ReflectionUtils.classExists(FAST_JSON_CLASS)) {
            return FastJsonJsonSerializer.getInstance();
        }
        if (ReflectionUtils.classExists(GSON_CLASS)) {
            return GsonJsonSerializer.getInstance();
        }
        throw new IllegalStateException("当前没有可用的JSON序列化器");
    }
}
