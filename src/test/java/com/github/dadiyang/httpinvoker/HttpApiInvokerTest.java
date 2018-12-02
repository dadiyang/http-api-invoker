package com.github.dadiyang.httpinvoker;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.annotation.HttpReq;
import com.github.dadiyang.httpinvoker.entity.City;
import com.github.dadiyang.httpinvoker.entity.ResultBean;
import com.github.dadiyang.httpinvoker.interfaces.CityService;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * HttpApiInvoker 完整单元测试
 *
 * @author huangxuyang
 * date 2018/11/27
 */
public class HttpApiInvokerTest {
    private Requestor requestor;
    private CityService service;
    private String host;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(HttpApiInvokerTest.class.getClassLoader().getResourceAsStream("conf.properties"));
        host = properties.get("api.url.city.host").toString();
        System.out.println("Mock requestor");
        requestor = mock(Requestor.class);
        service = new HttpApiProxyFactory(requestor, properties).getProxy(CityService.class);
    }

    @Test
    public void saveCitiesTest() throws Exception {
        System.out.println("————————————开始测试批量保存城市（集合类或数组的参数数）————————————");
        HttpReq httpReq = CityService.class.getMethod("saveCities", List.class).getAnnotation(HttpReq.class);
        String url = host + httpReq.value();
        List<City> citiesToSave = Arrays.asList(new City(22, "南京"), new City(23, "武汉"));
        when(requestor.sendRequest(url, null, new Object[]{citiesToSave}, httpReq)).thenReturn("true");
        assertTrue(service.saveCities(citiesToSave));
        System.out.println("————————————测试批量保存城市通过（集合类或数组的参数数）————————————");
    }

    @Test
    public void getAllCitiesTest() throws Exception {
        System.out.println("————————————开始测试获取全部城市（使用URI）————————————");
        List<City> cityList = new ArrayList<>();
        cityList.add(new City(1, "北京"));
        cityList.add(new City(2, "上海"));
        cityList.add(new City(3, "广州"));
        cityList.add(new City(4, "深圳"));
        HttpReq httpReq = CityService.class.getMethod("getAllCities").getAnnotation(HttpReq.class);
        String url = host + httpReq.value();
        when(requestor.sendRequest(url, null, null, httpReq)).thenReturn(JSON.toJSONString(cityList));
        List<City> cities = service.getAllCities();
        assertTrue(cityList.containsAll(cities));
        assertTrue(cities.containsAll(cityList));
        assertEquals(cities.size(), cityList.size());
        for (City city : cities) {
            System.out.println(city);
        }
        System.out.println("————————————测试获取全部城市通过（使用URI）————————————");
    }

    @Test
    public void saveCityTest() throws Exception {
        System.out.println("————————————开始测试保存单个城市（通过method指定POST）————————————");
        HttpReq httpReq = CityService.class.getMethod("saveCity", Integer.class, String.class).getAnnotation(HttpReq.class);
        String url = host + httpReq.value();
        Map<String, Object> param = new HashMap<>();
        param.put("id", 31);
        param.put("name", "东莞");
        when(requestor.sendRequest(url, param, new Object[]{31, "东莞"}, httpReq)).thenReturn("true");
        assertTrue(service.saveCity(31, "东莞"));
        System.out.println("————————————测试保存单个城市通过（通过method指定POST）————————————");
    }

    @Test
    public void getCityTest() throws Exception {
        System.out.println("————————————开始测试获取单个城市（使用Param注解指定方法参数）————————————");
        HttpReq httpReq = CityService.class.getMethod("getCity", int.class).getAnnotation(HttpReq.class);
        String url = host + httpReq.value();
        City city = new City(31, "东莞");
        Map<String, Object> param = new HashMap<>();
        param.put("id", 31);
        when(requestor.sendRequest(url, param, new Object[]{31}, httpReq)).thenReturn(JSON.toJSONString(city));
        assertEquals(service.getCity(31), city);
        System.out.println("————————————测试保存单个城市通过（使用Param注解指定方法参数）————————————");
    }

    @Test
    public void getCityByNameTest() throws Exception {
        System.out.println("————————————开始测试根据城市名查询城市（使用完整的路径）————————————");
        HttpReq httpReq = CityService.class.getMethod("getCityByName", String.class).getAnnotation(HttpReq.class);
        String url = httpReq.value();
        String name = "北京";
        ResultBean<City> expectedResult = new ResultBean<>(0, new City(1, name));
        Map<String, Object> param = new HashMap<>();
        param.put("name", "北京");
        when(requestor.sendRequest(url, param, new Object[]{"北京"}, httpReq)).thenReturn(JSON.toJSONString(expectedResult));
        ResultBean<City> invokedResult = service.getCityByName(name);
        assertEquals(expectedResult, invokedResult);
        City city = expectedResult.getData();
        assertEquals(city, invokedResult.getData());
        System.out.println("————————————测试根据城市名查询城市通过（使用完整的路径）————————————");
    }

    @Test
    public void getCityRest() throws Exception {
        System.out.println("————————————开始测试带有路径参数的方法————————————");
        HttpReq httpReq = CityService.class.getMethod("getCityRest", int.class).getAnnotation(HttpReq.class);
        int id = 1;
        String url = host + "/city/getCityRest/" + id;
        City city = new City(id, "北京");
        Map<String, Object> param = new HashMap<>();
        when(requestor.sendRequest(url, param, new Object[]{id}, httpReq)).thenReturn(JSON.toJSONString(city));
        assertEquals(city, service.getCityRest(id));
        System.out.println("————————————测试带有路径参数的方法通过————————————");
    }

    public void setRequestor(Requestor requestor) {
        this.requestor = requestor;
    }

    public void setService(CityService service) {
        this.service = service;
    }

    public void setHost(String host) {
        this.host = host;
    }

}