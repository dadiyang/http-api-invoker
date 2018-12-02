package com.github.dadiyang.httpinvoker;

import com.github.dadiyang.httpinvoker.annotation.HttpApiScan;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackages = {"com.github.dadiyang.httpinvoker"})
@HttpApiScan(configPaths = "classpath:conf.properties")
public class TestApplication {

    @Bean
    public Requestor requestor() {
        return mock(Requestor.class);
    }
}
