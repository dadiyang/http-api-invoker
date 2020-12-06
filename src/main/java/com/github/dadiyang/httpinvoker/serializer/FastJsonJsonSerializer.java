package com.github.dadiyang.httpinvoker.serializer;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 基于 FastJson 的序列化器，仅在类路径中有 fastjson 且没有其他 Json 序列化器时使用
 * <p>
 * 默认首选的序列化器
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class FastJsonJsonSerializer implements JsonSerializer {
    private static final FastJsonJsonSerializer INSTANCE = new FastJsonJsonSerializer();

    public static FastJsonJsonSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public String serialize(Object object) {
        if (object == null) {
            return "";
        }
        return JSON.toJSONString(object);
    }

    @Override
    public <T> T parseObject(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    @Override
    public List<Object> parseArray(String json) {
        return JSON.parseArray(json);
    }

    @Override
    public Map<String, Object> toMap(String json) {
        return JSON.parseObject(json);
    }
}
