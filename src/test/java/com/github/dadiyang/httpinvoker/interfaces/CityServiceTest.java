package com.github.dadiyang.httpinvoker.interfaces;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.dadiyang.httpinvoker.HttpApiProxyFactory;
import com.github.dadiyang.httpinvoker.entity.City;
import com.github.dadiyang.httpinvoker.entity.ComplicatedInfo;
import com.github.dadiyang.httpinvoker.entity.ResultBean;
import com.github.dadiyang.httpinvoker.entity.ResultBeanWithStatusAsCode;
import com.github.dadiyang.httpinvoker.requestor.*;
import com.github.dadiyang.httpinvoker.util.CityUtil;
import com.github.dadiyang.httpinvoker.util.ParamUtils;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.dadiyang.httpinvoker.util.CityUtil.createCities;
import static com.github.dadiyang.httpinvoker.util.CityUtil.createCity;
import static com.github.dadiyang.httpinvoker.util.IoUtils.closeStream;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class CityServiceTest {
    private static final int PORT = 18888;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT));
    private CityService cityService;
    private CityService cityServiceWithResultBeanResponseProcessor;
    private Requestor requestor;
    private String authKey;

    public CityServiceTest(Requestor requestor) {
        this.requestor = requestor;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<Object[]>();
        data.add(new Object[]{new JsoupRequestor()});
        data.add(new Object[]{new DefaultHttpRequestor()});
        data.add(new Object[]{new HttpClientRequestor()});
        return data;
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("api.url.city.host", "http://localhost:" + PORT);
        System.setProperty("api.url.city.host2", "http://localhost:" + PORT);

        HttpApiProxyFactory httpApiProxyFactory = new HttpApiProxyFactory.Builder().setRequestor(requestor).build();
        cityService = httpApiProxyFactory.getProxy(CityService.class);
        cityServiceWithResultBeanResponseProcessor = new HttpApiProxyFactory.Builder()
                .setRequestor(requestor)
                .setResponseProcessor(new ResultBeanResponseProcessor())
                .build()
                .getProxy(CityService.class);
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
        Map<String, Object> body = new HashMap<String, Object>();
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
        String uri = "/city/getCityByName?name=" + URLEncoder.encode(cityName, "UTF-8");
        City city = createCity(cityName);
        ResultBean<City> mockCityResult = new ResultBean<City>(0, city);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCityResult))));
        ResultBean<City> cityResultBean = cityService.getCityByName(cityName);
        assertEquals(mockCityResult, cityResultBean);

        // 测试如果返回值是 resultBean 时，尽管添加了 ResultBeanResponseProcessor 也不做特殊处理
        cityResultBean = cityServiceWithResultBeanResponseProcessor.getCityByName(cityName);
        assertEquals("返回值是 resultBean 时，ResultBeanResponseProcessor 不应做特殊处理", mockCityResult, cityResultBean);
    }

    @Test
    public void testWithConfigPathVariable() throws UnsupportedEncodingException {
        String cityName = "北京";
        String uri = "/city/getCityByName?name=" + URLEncoder.encode(cityName, "UTF-8") + "&id=";
        City city = createCity(cityName);
        ResultBean<City> mockCityResult = new ResultBean<City>(0, city);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCityResult))));
        ResultBean<City> cityResultBean = cityService.getCityByNameWithConfigVariable();
        assertEquals(mockCityResult, cityResultBean);

        // 测试如果返回值是 resultBean 时，尽管添加了 ResultBeanResponseProcessor 也不做特殊处理
        cityResultBean = cityServiceWithResultBeanResponseProcessor.getCityByNameWithConfigVariable();
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
    public void getCityRestWithDefaultPathVal() {
        int id = 1;
        String uri = "/city/getCityRest/" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri)).willReturn(aResponse().withBody(JSON.toJSONString(mockCity))));
        City result = cityService.getCityRestWithDefaultPathVal(null);
        assertEquals(mockCity, result);
    }

    @Test
    public void updateCity() {
        int id = 1;
        String name = "北京";
        String uri = "/city/" + id;
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("id", id);
        body.put("name", name);
        wireMockRule.stubFor(put(urlEqualTo(uri))
                .withRequestBody(equalToJson(JSON.toJSONString(body)))
                .willReturn(aResponse().withBody("true")));
        boolean result = cityService.updateCity(id, name);
        assertTrue(result);
    }

    @Test
    public void patchCity() {
        int id = 1;
        String name = "北京";
        String uri = "/city/patchCity";
        City city = new City(id, name);
        if (requestor instanceof JsoupRequestor) {
            // Jsoup does not support real PATCH method, but it can using X-HTTP-Method-Override header to send a fake PATCH
            wireMockRule.stubFor(post(urlEqualTo(uri))
                    .withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                    .willReturn(aResponse().withBody("true")));
        } else {
            wireMockRule.stubFor(patch(urlEqualTo(uri))
                    .withRequestBody(equalToJson(JSON.toJSONString(city)))
                    .willReturn(aResponse().withBody("true")));
        }
        boolean result = cityService.patchCity(city);
        assertTrue(result);
    }

    @Test
    public void testHead() {
        String uri = "/city/head";
        wireMockRule.stubFor(WireMock.head(urlEqualTo(uri)).willReturn(ok()));
        cityService.head();
    }

    @Test
    public void testTrace() {
        String uri = "/city/trace";
        wireMockRule.stubFor(trace(urlEqualTo(uri)).willReturn(aResponse().withBody("true")));
        boolean result = cityService.trace();
        assertTrue(result);
    }

    @Test
    public void testOptions() {
        String uri = "/city/options";
        wireMockRule.stubFor(WireMock.options(urlEqualTo(uri)).willReturn(aResponse().withBody("true")));
        boolean result = cityService.options();
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
        Map<String, String> headers = new HashMap<String, String>();
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
        Map<String, String> cookies = new HashMap<String, String>();
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
        String cookie1 = "c1=c; path=\"/\", c2=cc; domain=\"localhost\"";
        String cookie2 = "c3=ccc; path=\"/\", c2=cc; domain=\"localhost\"";
        wireMockRule.stubFor(get(urlEqualTo(uri))
                // 带了 header 的响应
                .willReturn(okJson(JSON.toJSONString(mockCities))
                        .withHeader(headerKey, headerValue)
                        .withHeader("Set-Cookie", cookie1)
                        .withHeader("Set-Cookie", cookie2)));
        HttpResponse response = cityService.listCity();
        System.out.println("获取到headers:" + response.getHeaders());
        assertEquals(headerValue, response.getHeader(headerKey));
        assertEquals("application/json", response.getHeader("Content-Type"));
        assertEquals("c", response.getCookie("c1"));
        assertEquals("ccc", response.getCookie("c3"));

        Map<String, List<String>> headers = response.multiHeaders();
        System.out.println(headers);
        assertEquals(headers.get("Content-Type"), Collections.singletonList("application/json"));
        assertEquals(headers.get("Set-Cookie"), Arrays.asList(cookie1, cookie2));
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            List<String> setCookieHeaders = response.getHeaders(entry.getKey());
            System.out.println(setCookieHeaders);
            assertEquals(setCookieHeaders, entry.getValue());
        }
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
                    .willReturn(ok().withBody(JSON.toJSONString(new ResultBean<String>(0, "OK")))));
            // 没有返回值的方法，只要不报错就可以
            cityService.deleteCity(id);
            cityServiceWithResultBeanResponseProcessor.deleteCity(id);
        }
    }

    @Test
    public void uploadTest() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        upload();
                        multipartTest();
                    } catch (Exception e) {
                        fail(e.getMessage());
                    }
                    return null;
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.MINUTES);
    }

    @Test
    public void upload() throws IOException {
        String uri = "/city/picture/upload";
        String randomName = UUID.randomUUID().toString();
        String fileName = "conf.properties";
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(fileName);
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
        } finally {
            closeStream(in);
        }
    }

    @Test
    public void multipartTest() throws IOException, InterruptedException {
        String uri = "/city/files/upload";
        String randomName = UUID.randomUUID().toString();
        String fileName1 = "conf.properties";
        String fileName2 = "conf2.properties";
        InputStream in1 = null;
        InputStream in2 = null;
        try {
            in1 = getClass().getClassLoader().getResourceAsStream(fileName1);
            in2 = getClass().getClassLoader().getResourceAsStream(fileName2);
            byte[] bytes1 = new byte[in1.available()];
            in1.read(bytes1);

            byte[] bytes2 = new byte[in2.available()];
            in2.read(bytes2);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append(UUID.randomUUID());
            }
            in1.read(sb.toString().getBytes());
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
        } finally {
            closeStream(in1);
            closeStream(in2);
        }
    }

    @Test
    public void preprocessorTest() {
        HttpApiProxyFactory factory = new HttpApiProxyFactory(new RequestPreprocessor() {
            @Override
            public void process(HttpRequest request) {
                request.addCookie("authCookies", authKey);
                request.addHeader("authHeaders", authKey);
                Method method = CURRENT_METHOD_THREAD_LOCAL.get();
                System.out.println("current method " + method.getName());
                try {
                    assertEquals(CityService.class.getMethod("getCity", int.class), method);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    fail("");
                }
            }
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
        assertNull("当前方法ThreadLocal应及时清理", RequestPreprocessor.CURRENT_METHOD_THREAD_LOCAL.get());
        assertEquals(mockCity, city);
    }

    @Test
    public void responseProcessTest() {
        ResponseProcessor cityResultProcessor = new ResponseProcessor() {
            @Override
            public Object process(HttpResponse response, Method method) {
                ResultBean<City> cityResultBean = JSON.parseObject(response.getBody(), new TypeReference<ResultBean<City>>() {
                });
                return cityResultBean.getData();
            }
        };
        HttpApiProxyFactory factory = new HttpApiProxyFactory(cityResultProcessor);
        CityService cityServiceWithResponseProcessor = factory.getProxy(CityService.class);
        int id = 1;
        String uri = "/city/getById?id=" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(new ResultBean<City>(0, mockCity)))));
        City city = cityServiceWithResponseProcessor.getCity(id);
        assertEquals(mockCity, city);
    }

    @Test
    public void getAllCitiesWithResultBeanResponseProcessor() throws NoSuchMethodException {
        List<City> mockCities = createCities();
        String uri = "/city/allCities";
        wireMockRule.stubFor(get(urlEqualTo(uri)).willReturn(aResponse().withBody(JSON.toJSONString(new ResultBean<List<City>>(0, mockCities)))));
        List<City> cityList = cityServiceWithResultBeanResponseProcessor.getAllCities();
        assertTrue(mockCities.containsAll(cityList));
        assertTrue(cityList.containsAll(mockCities));
    }

    @Test
    public void getCityWithResultBean() throws UnsupportedEncodingException {
        String cityName = "北京";
        String uri = "/city/getCityByName?name=" + URLEncoder.encode(cityName, "UTF-8");
        City city = createCity(cityName);
        ResultBean<City> mockCityResult = new ResultBean<City>(1, city);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCityResult))));
        City result = cityServiceWithResultBeanResponseProcessor.getCityWithResultBean(cityName);
        assertEquals(city, result);
    }

    @Test
    public void getCityWithStatusCode() throws UnsupportedEncodingException {
        String cityName = "北京";
        String uri = "/city/getCityByName?name=" + URLEncoder.encode(cityName, "UTF-8");
        City city = createCity(cityName);
        ResultBeanWithStatusAsCode<City> mockCityResult = new ResultBeanWithStatusAsCode<City>(1, city);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCityResult))));
        City result = cityServiceWithResultBeanResponseProcessor.getCityWithStatusCode(cityName);
        assertEquals(city, result);
    }

    @Test
    public void getCityObject() throws UnsupportedEncodingException {
        String uri = "/city/getCityObject";
        String cityString = JSON.toJSONString(new ResultBean<String>(0, "123"));
        wireMockRule.stubFor(get(urlEqualTo(uri))
                // 加在类上的头和cookie
                .withHeader("globalHeader1", equalTo("ok"))
                .withHeader("globalHeader2", equalTo("yes"))
                .withHeader("h3", equalTo("haha"))
                .withCookie("globalCookie", equalTo("ok"))
                // 类上的 UserAgent 注解
                .withHeader("User-Agent", equalTo("JUnit"))
                .willReturn(aResponse().withBody(cityString)));
        Object obj = cityService.getCityObject();
        assertEquals(obj, cityString);
        obj = cityServiceWithResultBeanResponseProcessor.getCityObject();
        assertEquals("123", obj);
    }

    @Test
    public void getCityName() {
        String uri = "/city/getCityName";
        String cityString = JSON.toJSONString(new ResultBean<String>(0, "北京"));
        wireMockRule.stubFor(get(urlPathEqualTo(uri))
                .withQueryParam("id", equalTo("1"))
                // 加在类上的头和cookie，如果key相同会被方法上的覆盖
                .withHeader("happy", equalTo("done"))
                .withHeader("h3", equalTo("nice"))
                .withHeader("globalHeader1", equalTo("ok"))
                .withHeader("globalHeader2", equalTo("yes"))
                .withCookie("globalCookie", equalTo("bad"))
                .withCookie("auth", equalTo("ok"))
                // 类上的 UserAgent 注解，会被方法上的注解覆盖
                .withHeader("User-Agent", equalTo("cityAgent"))
                .withHeader("Content-Type", equalTo("text/plain"))
                .willReturn(aResponse().withBody(cityString)));
        Object obj = cityService.getCityName(1);
        assertEquals(obj, cityString);
        obj = cityServiceWithResultBeanResponseProcessor.getCityName(1);
        assertEquals("北京", obj);
    }

    @Test
    public void getCityByComplicatedInfo() throws UnsupportedEncodingException {
        String uri = "/city/getCityByComplicatedInfo";
        List<City> cities = CityUtil.createCities();
        City city = CityUtil.createCity(1);
        ComplicatedInfo info = new ComplicatedInfo(cities, "OK", city);
        Map<String, String> map = ParamUtils.toMapStringString(info, "");
        StringBuilder expectedRequestBody = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            expectedRequestBody.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
        }
        wireMockRule.stubFor(post(urlPathEqualTo(uri))
                .withRequestBody(equalTo(expectedRequestBody.substring(0, expectedRequestBody.length() - 1)))
                .willReturn(aResponse().withBody(JSON.toJSONString(city))));
        City rs = cityService.getCityByComplicatedInfo(info);
        assertEquals(city, rs);
    }

    @Test
    public void getDate() throws UnsupportedEncodingException {
        String uri = "/city/date";
        Map<String, Object> param = new HashMap<String, Object>();
        Date now = new Date();
        param.put("date", now);
        wireMockRule.stubFor(post(urlPathEqualTo(uri))
                .withRequestBody(equalToJson(JSON.toJSONString(param)))
                .willReturn(aResponse().withBody(JSON.toJSONString(now))));
        Date date = cityService.getDate(now);
        assertEquals(date.getTime(), now.getTime());

        ResultBean<Date> resultBean = new ResultBean<Date>(0, now);
        wireMockRule.stubFor(post(urlPathEqualTo(uri))
                .withRequestBody(equalToJson(JSON.toJSONStringWithDateFormat(param, "")))
                .willReturn(aResponse().withBody(JSON.toJSONString(resultBean))));
        date = cityServiceWithResultBeanResponseProcessor.getDate(now);
        assertEquals(date.getTime(), now.getTime());
    }

    @Test
    public void getString() {
        String uri = "/city/string";
        String rs = "OK";
        wireMockRule.stubFor(get(urlPathEqualTo(uri)).willReturn(aResponse().withBody(rs)));
        String str = cityService.getString();
        assertEquals(rs, str);
        str = cityServiceWithResultBeanResponseProcessor.getString();
        assertEquals(rs, str);
    }
}