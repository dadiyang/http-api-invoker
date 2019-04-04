package com.github.dadiyang.httpinvoker.interfaces;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.TestApplication;
import com.github.dadiyang.httpinvoker.entity.City;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.dadiyang.httpinvoker.util.CityUtil.createCity;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestApplication.class)
public class CityServiceSpringTest {
    private static final int PORT = 18888;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT));
    @Autowired
    private CityService cityService;

    @Test
    public void getCity() {
        System.out.println(cityService.toString());
        int id = 1;
        String uri = "/city/getById?id=" + id;
        City mockCity = createCity(id);
        wireMockRule.stubFor(get(urlEqualTo(uri))
                .withCookie("testCookie", equalTo("OK"))
                .withHeader("testHeader", equalTo("OK"))
                .willReturn(aResponse().withBody(JSON.toJSONString(mockCity))));
        City city = cityService.getCity(id);
        assertEquals(mockCity, city);
    }
}
