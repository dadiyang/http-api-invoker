package com.github.dadiyang.httpinvoker.interfaces;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.HttpApiProxyFactory;
import com.github.dadiyang.httpinvoker.entity.City;
import com.github.dadiyang.httpinvoker.mocker.MockRequestor;
import com.github.dadiyang.httpinvoker.mocker.MockResponse;
import com.github.dadiyang.httpinvoker.mocker.MockRule;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.github.dadiyang.httpinvoker.util.IoUtils.closeStream;
import static org.junit.Assert.assertEquals;

/**
 * MockRequestor 单测
 *
 * @author dadiyang
 * @since 2019-05-31
 */
public class CityServiceMockRequestorTest {
    private CityService cityService;
    private MockRequestor requestor;

    @Before
    public void setUp() throws Exception {
        requestor = new MockRequestor();
        InputStream in = null;
        try {
            in = CityServiceMockRequestorTest.class.getClassLoader().getResourceAsStream("conf.properties");
            // 通过 Builder 构建代理工厂，使用 MockRequestor 来接管发送请求的过程
            HttpApiProxyFactory factory = new HttpApiProxyFactory.Builder()
                    .setRequestor(requestor)
                    .addProperties(in)
                    .build();
            cityService = factory.getProxy(CityService.class);
        } finally {
            closeStream(in);
        }
    }

    @Test
    public void urlAndDataTest() {
        requestor.addRule(new MockRule("http://localhost:18888/city/getCityName", Collections.singletonMap("id", (Object) 1), new MockResponse(200, "北京")));
        String name = cityService.getCityName(1);
        assertEquals("北京", name);
    }

    @Test
    public void urlRegTest() {
        requestor.addRule(new MockRule("http://localhost:18888/city/.*", Collections.singletonMap("id", (Object) 1), new MockResponse(200, "北京")));
        String name = cityService.getCityName(1);
        assertEquals("北京", name);
    }

    @Test
    public void uriTest() {
        int id = nextInt();
        City city = new City(id, "北京");
        MockRule rule = new MockRule();
        rule.setUriReg("/city/getCityRest/" + id);
        rule.setResponse(new MockResponse(200, JSON.toJSONString(city)));
        requestor.addRule(rule);
        City rs = cityService.getCityWithHeaders(id, genMap());
        assertEquals(city, rs);
    }

    private int nextInt() {
        Random r = new Random();
        return r.nextInt();
    }

    @Test
    public void headerTest() {
        int id = nextInt();
        City city = new City(id, "北京");
        MockRule rule = new MockRule("http://localhost:18888/city/getCityRest/" + id, "GET", new MockResponse(200, JSON.toJSONString(city)));
        Map<String, String> header = genMap();
        rule.setHeaders(header);

        // 请求头里还有规则之外的东西，匹配器应该忽略这些
        Map<String, String> requestHeader = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : header.entrySet()) {
            requestHeader.put(entry.getKey(), entry.getValue());
        }
        requestHeader.put("xxx", "1234");
        requestor.addRule(rule);
        City rs = cityService.getCityWithHeaders(id, requestHeader);
        assertEquals(city, rs);
    }

    @Test(expected = Exception.class)
    public void methodMismatch() {
        int id = nextInt();
        City city = new City(id, "北京");
        MockRule rule = new MockRule("http://localhost:18888/city/listCity" + id, "POST", new MockResponse(200, JSON.toJSONString(city)));
        requestor.addRule(rule);
        cityService.listCity();
    }

    private Map<String, String> genMap() {
        Map<String, String> header = new HashMap<String, String>();
        header.put("ttt", String.valueOf(nextInt()));
        header.put("ttt2", String.valueOf(nextInt()));
        return header;
    }

    @Test
    public void cookieTest() {
        int id = nextInt();
        City city = new City(id, "北京");
        MockRule rule = new MockRule("http://localhost:18888/city/getCityRest/" + id, new MockResponse(200, JSON.toJSONString(city)));
        Map<String, String> cookies = genMap();
        rule.setCookies(cookies);
        requestor.addRule(rule);
        City rs = cityService.getCityWithCookies(id, cookies);
        assertEquals(city, rs);
    }

    @Test
    public void headerAndCookieTest() {
        int id = nextInt();
        City city = new City(id, "北京");
        MockRule rule = new MockRule("http://localhost:18888/city/getCityRest/" + id, new MockResponse(200, JSON.toJSONString(city)));
        Map<String, String> cookies = genMap();
        Map<String, String> headers = genMap();
        rule.setCookies(cookies);
        rule.setHeaders(headers);
        requestor.addRule(rule);
        City rs = cityService.getCityWithCookiesAndHeaders(id, cookies, headers);
        assertEquals(city, rs);
    }

    @Test(expected = IllegalStateException.class)
    public void getCityNameMultiMockRuleTest() {
        requestor.addRule(new MockRule("http://localhost:18888/city/getCityName", Collections.singletonMap("id", (Object) 1), new MockResponse(200, "北京")));
        requestor.addRule(new MockRule("http://localhost:18888/city/getCityName", Collections.singletonMap("id", (Object) 1), new MockResponse(200, "北京")));
        String name = cityService.getCityName(1);
        assertEquals("北京", name);
    }
}
