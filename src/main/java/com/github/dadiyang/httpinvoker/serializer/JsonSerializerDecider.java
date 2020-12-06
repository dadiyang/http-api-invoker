package com.github.dadiyang.httpinvoker.serializer;

import com.github.dadiyang.httpinvoker.util.ReflectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认依次检测当前类路径是否有 FastJson 和 Gson 以决定采用哪种实现
 * <p>
 * 使用者可以通过 registerJsonSerializer 注册自己指定的 Json 实现，然后调用 setJsonInstanceKey 指定已注册的 Json 实现
 *
 * @author dadiyang
 * @since 2020/12/5
 */
public class JsonSerializerDecider {
    private static final String FAST_JSON_CLASS = "com.alibaba.fastjson.JSON";
    private static final String GSON_CLASS = "com.google.gson.Gson";
    private static Map<String, JsonSerializer> map = new ConcurrentHashMap<String, JsonSerializer>();
    private static String jsonInstanceKey;

    /**
     * 根据规则获取JSON序列化器实例
     *
     * @return JSON序列化器实例
     */
    public static JsonSerializer getJsonSerializer() {
        if (jsonInstanceKey != null) {
            JsonSerializer instance = map.get(jsonInstanceKey);
            if (instance == null) {
                throw new IllegalStateException("已指定了JSON序列化实现为: " + jsonInstanceKey + "，但是没有实际注册这个实现，请调用 registerJsonSerializer 方法先注册");
            }
            return instance;
        }
        return getDefaultInstance();
    }

    private static JsonSerializer getDefaultInstance() {
        // 默认使用 FAST_JSON
        if (ReflectionUtils.classExists(FAST_JSON_CLASS)) {
            return FastJsonJsonSerializer.getInstance();
        }
        if (ReflectionUtils.classExists(GSON_CLASS)) {
            return GsonJsonSerializer.getInstance();
        }
        throw new IllegalStateException("当前没有可用的JSON序列化器");
    }

    public static String getJsonInstanceKey() {
        return jsonInstanceKey;
    }

    /**
     * 指定使用哪一个 jsonSerializer 实现，使用这个特性必须先使用 registerJsonSerializer 方法把这个key对应的序列化进行注册，否则无法正常使用
     *
     * @param jsonInstanceKey 实例key
     */
    public static void setJsonInstanceKey(String jsonInstanceKey) {
        JsonSerializerDecider.jsonInstanceKey = jsonInstanceKey;
    }

    /**
     * 注册 json 序列化器
     *
     * @param key            实例key
     * @param jsonSerializer 序列化器实例
     */
    public static void registerJsonSerializer(String key, JsonSerializer jsonSerializer) {
        map.put(key, jsonSerializer);
    }
}
