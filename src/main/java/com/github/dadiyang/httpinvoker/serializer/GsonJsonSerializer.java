package com.github.dadiyang.httpinvoker.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 基于 gson 的 json 序列化器，仅在类路径中有 Gson 并且没有注册其他的 json 序列化器时使用
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class GsonJsonSerializer implements JsonSerializer {
    private static final Gson GSON = new GsonBuilder()
            .create();

    private static final GsonJsonSerializer INSTANCE = new GsonJsonSerializer();

    public static GsonJsonSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public String serialize(Object object) {
        return GSON.toJson(object);
    }

    @Override
    public <T> T parseObject(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    @Override
    public List<Object> parseArray(String json) {
        Type type = new TypeToken<List<Object>>() {
        }.getType();
        return GSON.fromJson(json, type);
    }

    @Override
    public Map<String, Object> toMap(String json) {
        return GSON.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

}
