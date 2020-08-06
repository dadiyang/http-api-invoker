package com.github.dadiyang.httpinvoker.interfaces;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.HttpApiProxyFactory;
import com.github.dadiyang.httpinvoker.entity.City;
import com.github.dadiyang.httpinvoker.entity.ResultBean;
import com.github.dadiyang.httpinvoker.entity.ResultBeanWithStatusAsCode;
import com.github.dadiyang.httpinvoker.requestor.ResultBeanResponseProcessor;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.github.dadiyang.httpinvoker.util.CityUtil.createCities;
import static com.github.dadiyang.httpinvoker.util.CityUtil.createCity;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

/**
 * 对一些错误值进行测试
 *
 * @author dadiyang
 * date 2019/1/10
 */
public class CityServiceErrorTest {
    private static final int PORT = 18888;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT));
    private CityService cityService;

    @Before
    public void setUp() throws Exception {
        System.setProperty("api.url.city.host", "http://localhost:" + PORT);
        System.setProperty("api.url.city.host2", "http://localhost:" + PORT);
        HttpApiProxyFactory httpApiProxyFactory = new HttpApiProxyFactory();
        cityService = httpApiProxyFactory.getProxy(CityService.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalPathVariable() {
        cityService.getCityRest(null);
    }

    /**
     * 测试类上面加的重试注解
     */
    @Test
    public void testClassAnnotatedRetry() {
        wireMockRule.stubFor(get(urlPathEqualTo("/city/getById")).willReturn(notFound()));
        try {
            cityService.getCity(1);
            fail("前面应该报异常");
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(e.getCause().getClass(), IOException.class);
        }
        // 前面报错后重试 3 次
        wireMockRule.verify(3, RequestPatternBuilder.allRequests().withUrl("/city/getById?id=1"));
    }

    /**
     * 测试方法上加的重试注解
     */
    @Test
    public void testMethodAnnotatedRetry() {
        long start = System.currentTimeMillis();
        wireMockRule.stubFor(get(urlPathEqualTo("/city/allCities")).willReturn(serverError()));
        try {
            cityService.getAllCities();
            fail("前面应该报异常");
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(e.getCause().getClass(), IOException.class);
        }
        long timeConsume = System.currentTimeMillis() - start;
        assertTrue("重试之前会休眠3000秒，因此2次请求，需要重试1次，即需要在3-6秒完成测试",
                timeConsume > 3000 && timeConsume < 6000);
        // 前面报错后重试 2 次
        wireMockRule.verify(2, RequestPatternBuilder.allRequests().withUrl("/city/allCities"));
    }

    /**
     * 只在50x的时候重试
     */
    @Test
    public void testOnlyRetry50x() {
        List<City> mockCities = createCities();
        String uri = "/city/save";
        wireMockRule.stubFor(post(urlPathEqualTo(uri)).willReturn(serverError()));
        try {
            cityService.saveCities(mockCities);
            fail("前面应该报异常");
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(e.getCause().getClass(), IOException.class);
        }
        // 前面报只对 50x 的错误尝试 2 次
        wireMockRule.verify(2, RequestPatternBuilder.allRequests().withUrl(uri));

        wireMockRule.resetAll();
        wireMockRule.stubFor(post(urlPathEqualTo(uri)).willReturn(notFound()));
        try {
            cityService.saveCities(mockCities);
            fail("前面应该报异常");
        } catch (Exception e) {
            assertEquals(e.getCause().getClass(), IOException.class);
        }
        // 前面报错后应该不重试
        wireMockRule.verify(1, RequestPatternBuilder.allRequests().withUrl(uri));
    }

    @Test(expected = IllegalStateException.class)
    public void getAllCitiesWithResultBeanResponseProcessor() {
        List<City> mockCities = createCities();
        String uri = "/city/allCities";
        wireMockRule.stubFor(get(urlEqualTo(uri)).willReturn(aResponse().withBody(JSON.toJSONString(new ResultBean<List<City>>(1, mockCities)))));
        CityService cityServiceWithResultBeanResponseProcessor = HttpApiProxyFactory.newProxy(CityService.class, new ResultBeanResponseProcessor());
        cityServiceWithResultBeanResponseProcessor.getAllCities();
    }

    @Test(expected = IllegalStateException.class)
    public void getCityWithResultBean() throws UnsupportedEncodingException {
        String cityName = "北京";
        String uri = "/city/getCityByName?name=" + URLEncoder.encode(cityName, "UTF-8");
        City city = createCity(cityName);
        ResultBean<City> mockCityResult = new ResultBean<City>(0, city);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCityResult))));
        CityService cityServiceWithResultBeanResponseProcessor = HttpApiProxyFactory.newProxy(CityService.class, new ResultBeanResponseProcessor());
        cityServiceWithResultBeanResponseProcessor.getCityWithResultBean(cityName);
    }

    @Test(expected = IllegalStateException.class)
    public void getCityWithStatusCode() throws UnsupportedEncodingException {
        String cityName = "北京";
        String uri = "/city/getCityByName?name=" + URLEncoder.encode(cityName, "UTF-8");
        ResultBeanWithStatusAsCode<City> mockCityResult = new ResultBeanWithStatusAsCode<City>("出错啦~", 0);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCityResult))));
        CityService cityServiceWithResultBeanResponseProcessor = HttpApiProxyFactory.newProxy(CityService.class, new ResultBeanResponseProcessor());
        cityServiceWithResultBeanResponseProcessor.getCityWithStatusCode(cityName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMethod() {
        cityService.invalidMethod();
    }

    @Test
    public void invalidMethodWithoutHttpReq() {
        try {
            cityService.invalidMethodWithoutHttpReq();
            fail("should throws an exception when invoke the method without annotated with @HttpReq");
        } catch (IllegalStateException e) {
            assertEquals(e.getMessage(), "this proxy only implement those HttpReq-annotated method, please add a @HttpReq on it.");
        }
    }
}
