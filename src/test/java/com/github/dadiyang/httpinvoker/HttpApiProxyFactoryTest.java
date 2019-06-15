package com.github.dadiyang.httpinvoker;

import com.github.dadiyang.httpinvoker.propertyresolver.MultiSourcePropertyResolver;
import com.github.dadiyang.httpinvoker.propertyresolver.PropertyResolver;
import com.github.dadiyang.httpinvoker.requestor.*;
import com.github.dadiyang.httpinvoker.util.ObjectUtils;
import org.junit.Test;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author dadiyang
 * @since 2019/5/20
 */
public class HttpApiProxyFactoryTest {
    /**
     * 工厂类测试
     */
    @Test
    public void testBuilder() {
        Requestor requestor = new Requestor() {
            @Override
            public HttpResponse sendRequest(HttpRequest request) throws IOException {
                return null;
            }
        };
        RequestPreprocessor requestPreprocessor = new RequestPreprocessor() {
            @Override
            public void process(HttpRequest request) {
            }
        };
        Properties properties = new Properties();
        properties.setProperty("Pro1", "OK");
        Environment environment = (Environment) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Environment.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (ObjectUtils.equals("getProperty", method.getName()) && args.length == 1) {
                    if (ObjectUtils.equals("Env1", args[0])) {
                        return "OK";
                    }
                }
                if (ObjectUtils.equals("containsProperty", method.getName())
                        && args.length == 1) {
                    return ObjectUtils.equals("Env1", args[0]);
                }

                if (ObjectUtils.equals("hashCode", method.getName())) {
                    return -1;
                }
                if (ObjectUtils.equals("equals", method.getName()) && args.length == 1) {
                    return true;
                }
                return null;
            }
        });
        PropertyResolver resolver = new PropertyResolver() {
            @Override
            public boolean containsProperty(String key) {
                return ObjectUtils.equals("PR1", key);
            }

            @Override
            public String getProperty(String key) {
                if (ObjectUtils.equals("PR1", key)) {
                    return "OK";
                }
                return null;
            }
        };
        ResponseProcessor responseProcessor = new ResponseProcessor() {
            @Override
            public Object process(HttpResponse response, Method method) {
                return null;
            }
        };
        HttpApiProxyFactory factory = new HttpApiProxyFactory.Builder()
                .setRequestor(requestor)
                .setRequestPreprocessor(requestPreprocessor)
                .setResponseProcessor(responseProcessor)
                .addProperties(properties)
                .addEnvironment(environment)
                .addPropertyResolver(resolver)
                .build();
        assertSame(requestor, factory.getRequestor());
        assertSame(requestPreprocessor, factory.getRequestPreprocessor());
        assertSame(responseProcessor, factory.getResponseProcessor());

        MultiSourcePropertyResolver resolvers = (MultiSourcePropertyResolver) factory.getPropertyResolver();
        assertEquals("OK", resolvers.getProperty("Pro1"));
        assertEquals("OK", resolvers.getProperty("Env1"));
        assertEquals("OK", resolvers.getProperty("PR1"));
    }
}