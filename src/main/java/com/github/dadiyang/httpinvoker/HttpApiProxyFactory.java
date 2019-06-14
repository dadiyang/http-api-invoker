package com.github.dadiyang.httpinvoker;

import com.github.dadiyang.httpinvoker.propertyresolver.EnvironmentBasePropertyResolver;
import com.github.dadiyang.httpinvoker.propertyresolver.MultiSourcePropertyResolver;
import com.github.dadiyang.httpinvoker.propertyresolver.PropertiesBasePropertyResolver;
import com.github.dadiyang.httpinvoker.propertyresolver.PropertyResolver;
import com.github.dadiyang.httpinvoker.requestor.DefaultHttpRequestor;
import com.github.dadiyang.httpinvoker.requestor.RequestPreprocessor;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import com.github.dadiyang.httpinvoker.requestor.ResponseProcessor;
import com.github.dadiyang.httpinvoker.util.IoUtils;
import com.github.dadiyang.httpinvoker.util.ObjectUtils;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory to create HttpApiInvoker
 *
 * @author huangxuyang
 * date 2018/10/30
 */
public class HttpApiProxyFactory {
    private Map<Class<?>, Object> instances = new ConcurrentHashMap<Class<?>, Object>();
    private Requestor requestor;
    private PropertyResolver propertyResolver;
    private RequestPreprocessor requestPreprocessor;
    private ResponseProcessor responseProcessor;

    /**
     * the builder of HttpApiProxyFactory
     *
     * @author dadiyang
     * @since 2019/5/20
     */
    public static class Builder {
        private Requestor requestor;
        private MultiSourcePropertyResolver propertyResolvers = new MultiSourcePropertyResolver();
        private RequestPreprocessor requestPreprocessor;
        private ResponseProcessor responseProcessor;

        public Builder setRequestor(Requestor requestor) {
            this.requestor = requestor;
            return this;
        }

        public Builder setRequestPreprocessor(RequestPreprocessor requestPreprocessor) {
            this.requestPreprocessor = requestPreprocessor;
            return this;
        }

        public Builder setResponseProcessor(ResponseProcessor responseProcessor) {
            this.responseProcessor = responseProcessor;
            return this;
        }

        public Builder addPropertyResolver(PropertyResolver propertyResolver) {
            this.propertyResolvers.addPropertyResolver(propertyResolver);
            return this;
        }

        public Builder addProperties(Properties properties) {
            propertyResolvers.addPropertyResolver(new PropertiesBasePropertyResolver(properties));
            return this;
        }

        public Builder addProperties(InputStream in) throws IOException {
            try {
                Properties properties = new Properties();
                properties.load(in);
                return addProperties(properties);
            } finally {
                in.close();
            }
        }

        public Builder addProperties(File file) throws IOException {
            ObjectUtils.requireNonNull(file, "properties file should not be null");
            Properties properties = IoUtils.getPropertiesFromFile(file.getAbsolutePath());
            return addProperties(properties);
        }

        public Builder addEnvironment(Environment environment) {
            propertyResolvers.addPropertyResolver(new EnvironmentBasePropertyResolver(environment));
            return this;
        }

        public HttpApiProxyFactory build() {
            HttpApiProxyFactory factory = new HttpApiProxyFactory();
            factory.requestor = requestor != null ? requestor : factory.requestor;
            factory.responseProcessor = responseProcessor != null ? responseProcessor : factory.responseProcessor;
            factory.requestPreprocessor = requestPreprocessor != null ? requestPreprocessor : factory.requestPreprocessor;
            propertyResolvers.addPropertyResolver(factory.propertyResolver);
            factory.propertyResolver = propertyResolvers;
            return factory;
        }
    }

    public HttpApiProxyFactory() {
        this(new DefaultHttpRequestor(), System.getProperties());
    }

    public HttpApiProxyFactory(Requestor requestor) {
        this(requestor, System.getProperties());
    }

    public HttpApiProxyFactory(Properties properties) {
        this(new DefaultHttpRequestor(), properties);
    }

    public HttpApiProxyFactory(Requestor requestor, Properties properties) {
        this(requestor, properties, null);
    }

    public HttpApiProxyFactory(Properties properties, RequestPreprocessor requestPreprocessor) {
        this(null, properties, requestPreprocessor);
    }

    public HttpApiProxyFactory(Properties properties, ResponseProcessor responseProcessor) {
        this(properties, null, responseProcessor);
    }

    public HttpApiProxyFactory(Properties properties, RequestPreprocessor requestPreprocessor, ResponseProcessor responseProcessor) {
        this(null, properties, requestPreprocessor, responseProcessor);
    }

    public HttpApiProxyFactory(RequestPreprocessor requestPreprocessor) {
        this(requestPreprocessor, null);
    }

    public HttpApiProxyFactory(ResponseProcessor responseProcessor) {
        this(System.getProperties(), responseProcessor);
    }

    public HttpApiProxyFactory(RequestPreprocessor requestPreprocessor, ResponseProcessor responseProcessor) {
        this(System.getProperties(), requestPreprocessor, responseProcessor);
    }


    public HttpApiProxyFactory(PropertyResolver propertyResolver) {
        this(new DefaultHttpRequestor(), propertyResolver);
    }

    public HttpApiProxyFactory(Requestor requestor, PropertyResolver propertyResolver) {
        this(requestor, propertyResolver, null);
    }

    public HttpApiProxyFactory(PropertyResolver propertyResolver, RequestPreprocessor requestPreprocessor) {
        this(null, propertyResolver, requestPreprocessor);
    }

    public HttpApiProxyFactory(PropertyResolver propertyResolver, ResponseProcessor responseProcessor) {
        this(null, propertyResolver, null, responseProcessor);
    }

    public HttpApiProxyFactory(PropertyResolver propertyResolver, RequestPreprocessor requestPreprocessor, ResponseProcessor responseProcessor) {
        this(null, propertyResolver, requestPreprocessor, responseProcessor);
    }

    public HttpApiProxyFactory(Requestor requestor,
                               Properties properties,
                               RequestPreprocessor requestPreprocessor) {
        this(requestor, properties, requestPreprocessor, null);
    }

    public HttpApiProxyFactory(Requestor requestor,
                               Properties properties,
                               RequestPreprocessor requestPreprocessor,
                               ResponseProcessor responseProcessor) {
        this(requestor, properties == null ? null : new PropertiesBasePropertyResolver(properties), requestPreprocessor, responseProcessor);
    }

    public HttpApiProxyFactory(Requestor requestor,
                               PropertyResolver propertyResolver,
                               RequestPreprocessor requestPreprocessor) {
        this(requestor, propertyResolver, requestPreprocessor, null);
    }

    public HttpApiProxyFactory(Requestor requestor,
                               PropertyResolver propertyResolver,
                               RequestPreprocessor requestPreprocessor,
                               ResponseProcessor responseProcessor) {
        this.requestor = requestor;
        this.propertyResolver = propertyResolver == null ? new PropertiesBasePropertyResolver(System.getProperties()) : propertyResolver;
        this.requestPreprocessor = requestPreprocessor;
        this.responseProcessor = responseProcessor;
    }

    public static <T> T newProxy(Class<T> clazz) {
        return newProxy(clazz, System.getProperties());
    }

    public static <T> T newProxy(Class<T> clazz, Properties properties) {
        return newProxy(clazz, null, properties);
    }

    public static <T> T newProxy(Class<T> clazz, RequestPreprocessor requestPreprocessor) {
        return newProxy(clazz, System.getProperties(), requestPreprocessor);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor) {
        return newProxy(clazz, requestor, System.getProperties());
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor, Properties properties) {
        return newProxy(clazz, requestor, properties, null);
    }

    public static <T> T newProxy(Class<T> clazz, Properties properties, RequestPreprocessor requestPreprocessor) {
        return newProxy(clazz, null, properties, requestPreprocessor);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor,
                                 Properties properties,
                                 RequestPreprocessor requestPreprocessor) {
        return newProxyInstance(requestor, properties, clazz, requestPreprocessor, null);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor,
                                 Properties properties,
                                 RequestPreprocessor requestPreprocessor, ResponseProcessor responseProcessor) {
        return newProxyInstance(requestor, properties, clazz, requestPreprocessor, responseProcessor);
    }

    public static <T> T newProxy(Class<T> clazz, PropertyResolver propertyResolver) {
        return newProxy(clazz, null, propertyResolver);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor, PropertyResolver propertyResolver) {
        return newProxy(clazz, requestor, propertyResolver, null);
    }

    public static <T> T newProxy(Class<T> clazz, PropertyResolver propertyResolver, RequestPreprocessor requestPreprocessor) {
        return newProxy(clazz, null, propertyResolver, requestPreprocessor);
    }

    public static <T> T newProxy(Class<T> clazz, PropertyResolver propertyResolver, RequestPreprocessor requestPreprocessor, ResponseProcessor responseProcessor) {
        return newProxy(clazz, null, propertyResolver, requestPreprocessor, responseProcessor);
    }

    public static <T> T newProxy(Class<T> clazz, PropertyResolver propertyResolver, ResponseProcessor responseProcessor) {
        return newProxy(clazz, null, propertyResolver, null, responseProcessor);
    }

    public static <T> T newProxy(Class<T> clazz, ResponseProcessor responseProcessor) {
        return newProxy(clazz, null, new PropertiesBasePropertyResolver(System.getProperties()), null, responseProcessor);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor,
                                 PropertyResolver propertyResolver,
                                 RequestPreprocessor requestPreprocessor) {
        return newProxyInstance(requestor, propertyResolver, clazz, requestPreprocessor, null);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor,
                                 PropertyResolver propertyResolver,
                                 RequestPreprocessor requestPreprocessor, ResponseProcessor responseProcessor) {
        return newProxyInstance(requestor, propertyResolver, clazz, requestPreprocessor, responseProcessor);
    }

    private static <T> T newProxyInstance(Requestor requestor, PropertyResolver propertyResolver,
                                          Class<?> clazz, RequestPreprocessor requestPreprocessor, ResponseProcessor responseProcessor) {
        InvocationHandler handler = new HttpApiInvoker(requestor, propertyResolver, clazz, requestPreprocessor, responseProcessor);
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, handler);
    }

    private static <T> T newProxyInstance(Requestor requestor, Properties properties,
                                          Class<?> clazz, RequestPreprocessor requestPreprocessor, ResponseProcessor responseProcessor) {
        InvocationHandler handler = new HttpApiInvoker(requestor, properties, clazz, requestPreprocessor, responseProcessor);
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, handler);
    }

    /**
     * dynamic proxy the given interface whose methods annotated with @HttpReq
     *
     * @param clazz an interface whose methods annotated with @HttpReq
     * @param <T>   this interface's type
     * @return the generated dynamic proxy
     * @throws IllegalStateException thrown when the method without annotated with @HttpReq was invoke
     */
    public <T> T getProxy(Class<T> clazz) {
        if (!instances.containsKey(clazz)) {
            synchronized (HttpApiProxyFactory.class) {
                if (!instances.containsKey(clazz)) {
                    instances.put(clazz, newProxyInstance(requestor, propertyResolver,
                            clazz, requestPreprocessor, responseProcessor));
                }
            }
        }
        //noinspection unchecked
        return (T) instances.get(clazz);
    }

    public Requestor getRequestor() {
        return requestor;
    }

    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    public RequestPreprocessor getRequestPreprocessor() {
        return requestPreprocessor;
    }

    public ResponseProcessor getResponseProcessor() {
        return responseProcessor;
    }
}
