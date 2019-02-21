package com.github.dadiyang.httpinvoker.spring;

import com.github.dadiyang.httpinvoker.HttpApiProxyFactory;
import com.github.dadiyang.httpinvoker.propertyresolver.PropertyResolver;
import com.github.dadiyang.httpinvoker.requestor.RequestPreprocessor;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import com.github.dadiyang.httpinvoker.requestor.ResponseProcessor;
import org.springframework.beans.factory.FactoryBean;

/**
 * A factory bean which produce HttpApi interface's implement by using proxyFactory
 *
 * @author huangxuyang
 * date 2018/11/1
 */
public class HttpApiProxyFactoryBean<T> implements FactoryBean<T> {
    private HttpApiProxyFactory proxyFactory;
    private Requestor requestor;
    private Class<T> interfaceClass;
    private PropertyResolver propertyResolver;
    private RequestPreprocessor requestPreprocessor;
    private ResponseProcessor responseProcessor;

    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setRequestor(Requestor requestor) {
        this.requestor = requestor;
    }

    public void setPropertyResolver(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }

    public void setRequestPreprocessor(RequestPreprocessor requestPreprocessor) {
        this.requestPreprocessor = requestPreprocessor;
    }

    public void setResponseProcessor(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public T getObject() throws Exception {
        if (proxyFactory == null) {
            proxyFactory = new HttpApiProxyFactory(requestor, propertyResolver, requestPreprocessor, responseProcessor);
        }
        return (T) proxyFactory.getProxy(interfaceClass);
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
