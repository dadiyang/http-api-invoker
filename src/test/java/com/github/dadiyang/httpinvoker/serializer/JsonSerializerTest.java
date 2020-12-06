package com.github.dadiyang.httpinvoker.serializer;

import com.github.dadiyang.httpinvoker.entity.City;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

/**
 * 本单测主要尽最大可能让不同的序列化实现互相兼容
 *
 * @author huangxuyang
 * @since 2020/12/6
 */
public class JsonSerializerTest {
    private GsonJsonSerializer gsonJsonSerializer = new GsonJsonSerializer();
    private FastJsonJsonSerializer fastJsonJsonSerializer = new FastJsonJsonSerializer();

    @Test
    public void serialize() {
        Date now = new Date();
        Map<String, Object> origMap = new LinkedHashMap<String, Object>();
        origMap.put("date", now);
        origMap.put("double", 1.1);
        origMap.put("integer", 1);
        origMap.put("arr", Arrays.asList(1, 2, 3, 4, 5));
        String gsonJson = gsonJsonSerializer.serialize(origMap);
        String fastJsonJson = fastJsonJsonSerializer.serialize(origMap);
        assertEquals("gson序列化结果应与fastJson序列化结果一致", gsonJson, fastJsonJson);
        String expectJson = "{\"date\":" + now.getTime() + ",\"double\":1.1,\"integer\":1,\"arr\":[1,2,3,4,5]}";
        assertEquals("序列化结果应与期望的一致", expectJson, gsonJson);

        Map<String, Object> map = gsonJsonSerializer.toMap(gsonJson);
        Map<String, Object> fastJsonMap = fastJsonJsonSerializer.toMap(gsonJson);

        assertMapEquals(map, fastJsonMap);

        assertEquals("再序列化回来，然后再序列化，应该结果一致", expectJson, gsonJsonSerializer.serialize(map));
    }

    private void assertMapEquals(Map<String, Object> m1, Map<String, Object> m2) {
        assertFalse("两个空map比对没有意义", m1.isEmpty());
        assertFalse("两个空map比对没有意义", m2.isEmpty());
        assertEquals("两个map的大小应该一致", m1.size(), m2.size());
        assertTrue("两个map的key应互相全包含", m1.keySet().containsAll(m2.keySet()));
        assertTrue("两个map的key应互相全包含", m2.keySet().containsAll(m1.keySet()));

        for (Map.Entry<String, Object> entry : m1.entrySet()) {
            assertEquals(entry.getKey() + "应相等", entry.getValue(), m2.get(entry.getKey()));
        }
    }

    @Test
    public void parseObject() {
        City city = new City();
        city.setId(1);
        city.setName("北京");

        String gsonJson = gsonJsonSerializer.serialize(city);
        String fastJsonJson = fastJsonJsonSerializer.serialize(city);
        assertEquals("gson序列化结果应与fastJson序列化结果一致", gsonJson, fastJsonJson);

        City gsonCity = gsonJsonSerializer.parseObject(gsonJson, City.class);
        City fastCity = fastJsonJsonSerializer.parseObject(gsonJson, City.class);
        assertEquals("gson反序列化结果应与fastJson反序列化结果一致", gsonCity, fastCity);

        assertEquals("序列化后再反序列化回来应该相等", city, gsonCity);
    }

    @Test
    public void parseArray() {
        String arrJson = "[1.0,2,3,4,5]";
        List<Object> gsonArr = gsonJsonSerializer.parseArray(arrJson);
        List<Object> fastJsonArr = fastJsonJsonSerializer.parseArray(arrJson);
        assertEquals(5, gsonArr.size());
        assertEquals(5, fastJsonArr.size());
        assertTrue("应包含指定的元素" + gsonArr, gsonArr.contains(new BigDecimal("1.0")));
        assertTrue("应包含指定的元素" + gsonArr, gsonArr.contains(2));
        assertTrue("应包含指定的元素" + gsonArr, gsonArr.contains(3));
        assertTrue("应包含指定的元素" + gsonArr, gsonArr.contains(4));
        assertTrue("应包含指定的元素" + gsonArr, gsonArr.contains(5));

        assertTrue("应包含指定的元素" + fastJsonArr, fastJsonArr.contains(new BigDecimal("1.0")));
        assertTrue("应包含指定的元素" + fastJsonArr, fastJsonArr.contains(2));
        assertTrue("应包含指定的元素" + fastJsonArr, fastJsonArr.contains(3));
        assertTrue("应包含指定的元素" + fastJsonArr, fastJsonArr.contains(4));
        assertTrue("应包含指定的元素" + fastJsonArr, fastJsonArr.contains(5));
    }
}