package com.github.dadiyang.httpinvoker.serializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 序列化器
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public interface JsonSerializer {
    /**
     * 将对象序列化为字符串
     *
     * @param object 对象
     * @return 字符串
     */
    String serialize(Object object);

    <T> T parseObject(String json, Type type);

    List<Object> parseArray(String json);

    Map<String, Object> toMap(String json);
}
