package com.github.dadiyang.httpinvoker.util;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.entity.ComplicatedInfo;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ParamUtilsTest {

    @Test
    public void toMapStringString() {
        ComplicatedInfo info = new ComplicatedInfo(CityUtil.createCities(), "123", CityUtil.createCity(1));
        System.out.println(JSON.toJSONString(info, true));
        Map<String, String> rs = ParamUtils.toMapStringString(info, "");
        System.out.println(rs);
        assertEquals("{msg=123, cities[3][id]=4, city[name]=北京, cities[2][id]=3, cities[1][id]=2, cities[2][name]=广州, city[id]=1, cities[0][name]=北京, cities[0][id]=1, cities[1][name]=上海, cities[3][name]=深圳}", rs.toString());
    }
}