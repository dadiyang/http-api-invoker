package com.github.dadiyang.httpinvoker;

import com.github.dadiyang.httpinvoker.annotation.HttpApiScan;
import com.github.dadiyang.httpinvoker.mocker.MockRequestor;
import com.github.dadiyang.httpinvoker.mocker.MockResponse;
import com.github.dadiyang.httpinvoker.mocker.MockRule;
import com.github.dadiyang.httpinvoker.requestor.HttpRequest;
import com.github.dadiyang.httpinvoker.requestor.RequestPreprocessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@HttpApiScan(configPaths = "classpath:conf.properties")
@PropertySource("classpath:conf2.properties")
public class TestApplication {

    @Bean
    public RequestPreprocessor requestPreprocessor() {
        return new RequestPreprocessor() {
            @Override
            public void process(HttpRequest request) {
                request.addHeader("testHeader", "OK");
                request.addCookie("testCookie", "OK");
            }
        };
    }

    @Bean
    public MockRequestor requestor() {
        MockRequestor requestor = new MockRequestor();
        MockRule rule = new MockRule("http://localhost:18888/city/getCityName", Collections.singletonMap("id", (Object) 1), new MockResponse(200, "北京"));
        requestor.addRule(rule);
        return requestor;
    }
}
