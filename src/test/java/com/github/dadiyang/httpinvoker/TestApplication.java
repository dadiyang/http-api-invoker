package com.github.dadiyang.httpinvoker;

import com.github.dadiyang.httpinvoker.annotation.HttpApiScan;
import com.github.dadiyang.httpinvoker.requestor.RequestPreprocessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@HttpApiScan(configPaths = "classpath:conf.properties")
@PropertySource("classpath:conf2.properties")
public class TestApplication {

    @Bean
    public RequestPreprocessor requestPreprocessor() {
        return request -> {
            request.addHeader("testHeader", "OK");
            request.addCookie("testCookie", "OK");
        };
    }
}
