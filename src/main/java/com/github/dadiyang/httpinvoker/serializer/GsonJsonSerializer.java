package com.github.dadiyang.httpinvoker.serializer;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            .registerTypeAdapter(new TypeToken<Map<String, Object>>() {
            }.getType(), NumberTypeAdapter.INSTANCE)
            .registerTypeAdapter(new TypeToken<List<Object>>() {
            }.getType(), NumberTypeAdapter.INSTANCE)
            .registerTypeAdapter(java.util.Date.class, new DateSerializer()).setDateFormat(DateFormat.LONG)
            .registerTypeAdapter(java.util.Date.class, new DateDeserializer()).setDateFormat(DateFormat.LONG)
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

    /**
     * 解决 int 类型序列化时变成 double 类型的问题
     */
    public static class NumberTypeAdapter extends TypeAdapter<Object> {
        private static final NumberTypeAdapter INSTANCE = new NumberTypeAdapter();

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<Object>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;
                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<String, Object>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;
                case STRING:
                    return in.nextString();
                case NUMBER:
                    // 改写数字的处理逻辑，将数字值分为整型与浮点型。
                    String str = in.nextString();
                    // 有小数点直接返回 BigDecimal 类弄浮点数
                    if (str.contains(".")) {
                        return new BigDecimal(str);
                    }
                    long lngNum = Long.parseLong(str);
                    if (lngNum > Integer.MAX_VALUE || lngNum < Integer.MIN_VALUE) {
                        return lngNum;
                    } else {
                        return (int) lngNum;
                    }
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void write(JsonWriter out, Object value) {
            // 序列化无需实现
        }
    }

    /**
     * 日期类型的字段反序列化器
     */
    public static class DateDeserializer implements JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(json.getAsJsonPrimitive().getAsLong());
        }
    }

    /**
     * 时间类型序列化为时间戳
     */
    public static class DateSerializer implements com.google.gson.JsonSerializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }
}
