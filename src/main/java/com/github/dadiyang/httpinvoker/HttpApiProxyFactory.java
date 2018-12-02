package com.github.dadiyang.httpinvoker;

import com.github.dadiyang.httpinvoker.requestor.DefaultHttpRequestor;
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

    public HttpApiProxyFactory(Requestor requestor) {
        this(requestor, System.getProperties());
    }

    public HttpApiProxyFactory(Properties properties) {
        this(new DefaultHttpRequestor(), properties);
    }

    public HttpApiProxyFactory(Requestor requestor, Properties properties) {
        this.requestor = requestor == null ? new DefaultHttpRequestor() : requestor;
        this.properties = properties == null ? System.getProperties() : properties;
    }

    public HttpApiProxyFactory() {
        this(new DefaultHttpRequestor(), System.getProperties());
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
                    InvocationHandler handler = new HttpApiInvoker(requestor, properties, clazz);
                    instances.put(clazz, Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, handler));
                }
            }
        }
        //noinspection unchecked
        return (T) instances.get(clazz);
    }
}
