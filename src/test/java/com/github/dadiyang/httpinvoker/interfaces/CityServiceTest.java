package com.github.dadiyang.httpinvoker.interfaces;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.dadiyang.httpinvoker.HttpApiProxyFactory;
import com.github.dadiyang.httpinvoker.entity.City;
import com.github.dadiyang.httpinvoker.entity.ResultBean;
import com.github.dadiyang.httpinvoker.requestor.HttpResponse;
import com.github.dadiyang.httpinvoker.requestor.MultiPart;
import com.github.dadiyang.httpinvoker.requestor.ResponseProcessor;
import com.github.dadiyang.httpinvoker.requestor.ResultBeanResponseProcessor;
import com.github.dadiyang.httpinvoker.util.CityUtil;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.github.dadiyang.httpinvoker.util.CityUtil.createCities;
import static com.github.dadiyang.httpinvoker.util.CityUtil.createCity;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

public class CityServiceTest {
    private static final int PORT = 18888;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT));
    private CityService cityService;
    private CityService cityServiceWithResultBeanResponseProcessor;

    private String authKey;

    @Before
    public void setUp() throws Exception {
        System.setProperty("api.url.city.host", "http://localhost:" + PORT);
        System.setProperty("api.url.city.host2", "http://localhost:" + PORT);
        HttpApiProxyFactory httpApiProxyFactory = new HttpApiProxyFactory();
        cityService = httpApiProxyFactory.getProxy(CityService.class);
        cityServiceWithResultBeanResponseProcessor = HttpApiProxyFactory.newProxy(CityService.class, new ResultBeanResponseProcessor());
        authKey = UUID.randomUUID().toString();
    }

    @Test
    public void getAllCities() throws NoSuchMethodException {
        List<City> mockCities = createCities();
        String uri = "/city/allCities";
        wireMockRule.stubFor(get(urlEqualTo(uri)).willReturn(aResponse().withBody(JSON.toJSONString(mockCities))));
        List<City> cityList = cityService.getAllCities();
        assertTrue(mockCities.containsAll(cityList));
        assertTrue(cityList.containsAll(mockCities));
    }

    @Test
    public void getCity() {
        int id = 1;
        String uri = "/city/getById?id=" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri)).willReturn(aResponse().withBody(JSON.toJSONString(mockCity))));
        City city = HttpApiProxyFactory.newProxy(CityService.class).getCity(id);
        assertEquals(mockCity, city);
    }

    @Test
    public void saveCities() {
        List<City> mockCities = createCities();
        String uri = "/city/save";
        wireMockRule.stubFor(post(urlEqualTo(uri)).withRequestBody(equalToJson(JSON.toJSONString(mockCities))).willReturn(aResponse().withBody("true")));
        boolean rs = cityService.saveCities(mockCities);
        assertTrue(rs);
    }

    @Test
    public void saveCity() {
        String uri = "/city/saveCity";
        Map<String, Object> body = new HashMap<>();
        int id = 1;
        String name = "北京";
        body.put("name", name);
        body.put("id", id);
        wireMockRule.stubFor(post(urlEqualTo(uri))
                .withRequestBody(equalToJson(JSON.toJSONString(body)))
                .willReturn(aResponse().withBody("true")));
        boolean rs = cityService.saveCity(id, name);
        assertTrue(rs);
    }

    @Test
    public void getCityByName() throws UnsupportedEncodingException {
        String cityName = "北京";
        String uri = "/city/getCityByName?name=" + URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        City city = createCity(cityName);
        ResultBean<City> mockCityResult = new ResultBean<>(0, city);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCityResult))));
        ResultBean<City> cityResultBean = cityService.getCityByName(cityName);
        assertEquals(mockCityResult, cityResultBean);

        // 测试如果返回值是 resultBean 时，尽管添加了 ResultBeanResponseProcessor 也不做特殊处理
        cityResultBean = cityServiceWithResultBeanResponseProcessor.getCityByName(cityName);
        assertEquals("返回值是 resultBean 时，ResultBeanResponseProcessor 不应做特殊处理", mockCityResult, cityResultBean);
    }

    @Test
    public void getCityRest() {
        int id = 1;
        String uri = "/city/getCityRest/" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri)).willReturn(aResponse().withBody(JSON.toJSONString(mockCity))));
        City result = cityService.getCityRest(id);
        assertEquals(mockCity, result);
    }

    @Test
    public void updateCity() {
        int id = 1;
        String name = "北京";
        String uri = "/city/" + id;
        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        body.put("name", name);
        wireMockRule.stubFor(put(urlEqualTo(uri))
                .withRequestBody(equalToJson(JSON.toJSONString(body)))
                .willReturn(aResponse().withBody("true")));
        boolean result = cityService.updateCity(id, name);
        assertTrue(result);
    }

    @Test
    public void getCityWithErrHeaders() {
        try {
            int id = 1;
            cityService.getCityWithErrHeaders(id, "");
            fail("getCityWithErrHeaders should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println("getCityWithErrHeaders");
        }
    }

    @Test
    public void getCityWithHeaders() {
        Map<String, String> headers = new HashMap<>();
        String key = "auth";
        String key2 = "auth2";
        headers.put(key, "123");
        headers.put(key2, "321");
        int id = 1;
        String uri = "/city/getCityRest/" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .withHeader(key, equalTo(headers.get(key)))
                .withHeader(key2, equalTo(headers.get(key2)))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCity))));
        City result = cityService.getCityWithHeaders(id, headers);
        assertEquals(mockCity, result);
    }

    @Test
    public void getCityWithCookies() {
        Map<String, String> cookies = new HashMap<>();
        String key = "auth";
        String key2 = "auth2";
        cookies.put(key, "123");
        cookies.put(key2, "321");
        int id = 1;
        String uri = "/city/getCityRest/" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .withCookie(key, equalTo(cookies.get(key)))
                .withCookie(key2, equalTo(cookies.get(key2)))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCity))));
        City result = cityService.getCityWithCookies(id, cookies);
        assertEquals(mockCity, result);
    }

    @Test
    public void listCity() {
        List<City> mockCities = createCities();
        String uri = "/city/listCity";
        String headerKey = "header1";
        String headerValue = "value11";
        wireMockRule.stubFor(get(urlEqualTo(uri))
                // 带了 header 的响应
                .willReturn(okJson(JSON.toJSONString(mockCities)).withHeader(headerKey, headerValue)));
        HttpResponse response = cityService.listCity();
        System.out.println("获取到headers:" + response.getHeaders());
        assertEquals(headerValue, response.getHeader(headerKey));
        assertEquals("application/json", response.getHeader("Content-Type"));
        List<City> cityList = JSON.parseArray(response.getBody(), City.class);
        assertTrue(mockCities.containsAll(cityList));
        assertTrue(cityList.containsAll(mockCities));
    }

    @Test
    public void hasCity() {
        int id = 1;
        City mockCity = createCity(id);
        String uri = "/city/getCity";
        wireMockRule.stubFor(get(urlPathEqualTo(uri))
                .withQueryParam("id", equalTo(String.valueOf(mockCity.getId())))
                .withQueryParam("name", equalTo(mockCity.getName()))
                .willReturn(aResponse().withBody("true")));
        boolean exists = cityService.hasCity(mockCity);
        assertTrue(exists);
    }

    /**
     * 测试表单提交
     */
    @Test
    public void saveCityForm() throws UnsupportedEncodingException {
        City city = CityUtil.createCity(1);
        String uri = "/city/saveCity";
        wireMockRule.stubFor(post(urlPathEqualTo(uri))
                .withRequestBody(equalTo("name=" + URLEncoder.encode(city.getName(), "utf-8") + "&id=" + city.getId()))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(aResponse().withBody("true")));
        boolean rs = cityService.saveCityForm(city);
        assertTrue(rs);
    }

    @Test
    public void getCities() {
        String uri = "/city/getByIds";
        List<Integer> cityIds = Arrays.asList(1, 2, 3);
        List<City> rs = CityUtil.getCities(cityIds);
        wireMockRule.stubFor(get(urlPathEqualTo(uri))
                .withQueryParam("id", equalTo("1"))
                .withQueryParam("id", equalTo("2"))
                .withQueryParam("id", equalTo("3"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withBody(JSON.toJSONString(rs))));
        List<City> cities = cityService.getCities(cityIds);
        assertEquals(rs, cities);
        System.out.println(cities);
    }

    @Test
    public void deleteCity() {
        for (int i = 0; i < 10; i++) {
            int id = 1;
            String uri = "/city/" + id;
            wireMockRule.stubFor(delete(urlPathEqualTo(uri))
                    .willReturn(ok().withBody(JSON.toJSONString(new ResultBean<>(0, "OK")))));
            // 没有返回值的方法，只要不报错就可以
            cityService.deleteCity(id);
            cityServiceWithResultBeanResponseProcessor.deleteCity(id);
        }
    }

    @Test
    public void upload() {
        String uri = "/city/picture/upload";
        String randomName = UUID.randomUUID().toString();
        String fileName = "conf.properties";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);) {
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            wireMockRule.stubFor(post(urlPathEqualTo(uri))
                    .withMultipartRequestBody(aMultipart("media").withBody(binaryEqualTo(bytes)))
                    .withMultipartRequestBody(aMultipart("fileName").withBody(equalTo(fileName)))
                    .willReturn(aResponse().withBody(randomName)));
            String name = cityService.upload(fileName, new ByteArrayInputStream(bytes));
            assertEquals(randomName, name);
        } catch (IOException e) {
            e.printStackTrace();
            fail("read test file error");
        }
    }

    @Test
    public void multipartTest() {
        String uri = "/city/files/upload";
        String randomName = UUID.randomUUID().toString();
        String fileName1 = "conf.properties";
        String fileName2 = "conf2.properties";
        try (InputStream in1 = getClass().getClassLoader().getResourceAsStream(fileName1);
             InputStream in2 = getClass().getClassLoader().getResourceAsStream(fileName2);) {

            byte[] bytes1 = new byte[in1.available()];
            in1.read(bytes1);

            byte[] bytes2 = new byte[in2.available()];
            in2.read(bytes2);

            wireMockRule.stubFor(post(urlPathEqualTo(uri))
                    .withMultipartRequestBody(aMultipart("conf1").withBody(binaryEqualTo(bytes1)))
                    .withMultipartRequestBody(aMultipart("conf2").withBody(binaryEqualTo(bytes2)))
                    .willReturn(aResponse().withBody(randomName)));

            MultiPart.Part part1 = new MultiPart.Part("conf1", fileName1, new ByteArrayInputStream(bytes1));
            MultiPart.Part part2 = new MultiPart.Part("conf2", fileName2, new ByteArrayInputStream(bytes2));

            MultiPart multiPart = new MultiPart();
            multiPart.addPart(part1);
            multiPart.addPart(part2);

            String name = cityService.multiPartForm(multiPart);
            assertEquals(randomName, name);
        } catch (IOException e) {
            e.printStackTrace();
            fail("submit multipart form error");
        }
    }

    @Test
    public void preprocessorTest() {
        HttpApiProxyFactory factory = new HttpApiProxyFactory(request -> {
            request.addCookie("authCookies", authKey);
            request.addHeader("authHeaders", authKey);
        });
        CityService cityServiceWithPreprocessor = factory.getProxy(CityService.class);
        int id = 1;
        String uri = "/city/getById?id=" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .withCookie("authCookies", equalTo(authKey))
                .withHeader("authHeaders", equalTo(authKey))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCity))));
        City city = cityServiceWithPreprocessor.getCity(id);
        assertEquals(mockCity, city);
    }

    @Test
    public void responseProcessTest() {
        ResponseProcessor cityResultProcessor = (response, method) -> {
            ResultBean<City> cityResultBean = JSON.parseObject(response.getBody(), new TypeReference<ResultBean<City>>() {
            });
            return cityResultBean.getData();
        };
        HttpApiProxyFactory factory = new HttpApiProxyFactory(cityResultProcessor);
        CityService cityServiceWithResponseProcessor = factory.getProxy(CityService.class);
        int id = 1;
        String uri = "/city/getById?id=" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(new ResultBean<>(0, mockCity)))));
        City city = cityServiceWithResponseProcessor.getCity(id);
        assertEquals(mockCity, city);
    }

    @Test
    public void getAllCitiesWithResultBeanResponseProcessor() throws NoSuchMethodException {
        List<City> mockCities = createCities();
        String uri = "/city/allCities";
        wireMockRule.stubFor(get(urlEqualTo(uri)).willReturn(aResponse().withBody(JSON.toJSONString(new ResultBean<>(0, mockCities)))));
        List<City> cityList = cityServiceWithResultBeanResponseProcessor.getAllCities();
        assertTrue(mockCities.containsAll(cityList));
        assertTrue(cityList.containsAll(mockCities));
    }

    @Test
    public void getCityWithResultBean() throws UnsupportedEncodingException {
        String cityName = "北京";
        String uri = "/city/getCityByName?name=" + URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        City city = createCity(cityName);
        ResultBean<City> mockCityResult = new ResultBean<>(1, city);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCityResult))));
        CityService cityServiceWithResultBeanResponseProcessor = HttpApiProxyFactory.newProxy(CityService.class, new ResultBeanResponseProcessor());
        City result = cityServiceWithResultBeanResponseProcessor.getCityWithResultBean(cityName);
        assertEquals(city, result);
    }

    @Test
    public void getCityObject() throws UnsupportedEncodingException {
        String uri = "/city/getCityObject";
        String cityString = JSON.toJSONString(new ResultBean<>(0, "123"));
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(cityString)));
        Object obj = cityService.getCityObject();
        assertEquals(obj, cityString);

        obj = cityServiceWithResultBeanResponseProcessor.getCityObject();
        assertEquals("123", obj);
    }

    @Test
    public void getCityName() {
        String uri = "/city/getCityName";
        String cityString = JSON.toJSONString(new ResultBean<>(0, "123"));
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(cityString)));
        Object obj = cityService.getCityName();
        assertEquals(obj, cityString);

        obj = cityServiceWithResultBeanResponseProcessor.getCityName();
        assertEquals("123", obj);
    }
}