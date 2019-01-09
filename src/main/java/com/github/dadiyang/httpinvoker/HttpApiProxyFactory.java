package com.github.dadiyang.httpinvoker;

import com.github.dadiyang.httpinvoker.requestor.DefaultHttpRequestor;
import com.github.dadiyang.httpinvoker.requestor.RequestPreprocessor;
import com.github.dadiyang.httpinvoker.requestor.Requestor;

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
    private Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
    private Requestor requestor;
    private Properties properties;
    private RequestPreprocessor requestPreprocessor;

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

    public HttpApiProxyFactory(RequestPreprocessor requestPreprocessor) {
        this(null, null, requestPreprocessor);
    }

    public HttpApiProxyFactory(Requestor requestor,
                               Properties properties,
                               RequestPreprocessor requestPreprocessor) {
        this.requestor = requestor;
        this.properties = properties;
        this.requestPreprocessor = requestPreprocessor;
    }

    public static <T> T newProxy(Class<T> clazz) {
        return newProxyInstance(null, null, clazz, null);
    }

    public static <T> T newProxy(Class<T> clazz, Properties properties) {
        return newProxyInstance(null, properties, clazz, null);
    }

    public static <T> T newProxy(Class<T> clazz, RequestPreprocessor requestPreprocessor) {
        return newProxyInstance(null, null, clazz, requestPreprocessor);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor) {
        return newProxyInstance(requestor, null, clazz, null);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor, Properties properties) {
        return newProxyInstance(requestor, properties, clazz, null);
    }

    public static <T> T newProxy(Class<T> clazz, Properties properties, RequestPreprocessor requestPreprocessor) {
        return newProxyInstance(null, properties, clazz, requestPreprocessor);
    }

    public static <T> T newProxy(Class<T> clazz, Requestor requestor,
                                 Properties properties,
                                 RequestPreprocessor requestPreprocessor) {
        return newProxyInstance(requestor, properties, clazz, requestPreprocessor);
    }

    private static <T> T newProxyInstance(Requestor requestor, Properties properties,
                                          Class<?> clazz, RequestPreprocessor requestPreprocessor) {
        InvocationHandler handler = new HttpApiInvoker(requestor, properties, clazz, requestPreprocessor);
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
                    instances.put(clazz, newProxyInstance(requestor, properties,
                            clazz, requestPreprocessor));
                }
            }
        }
        //noinspection unchecked
        return (T) instances.get(clazz);
    }
}
