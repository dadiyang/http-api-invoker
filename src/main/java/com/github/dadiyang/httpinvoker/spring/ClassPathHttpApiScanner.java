package com.github.dadiyang.httpinvoker.spring;

import com.github.dadiyang.httpinvoker.annotation.HttpApi;
import com.github.dadiyang.httpinvoker.propertyresolver.PropertyResolver;
import com.github.dadiyang.httpinvoker.requestor.RequestPreprocessor;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import com.github.dadiyang.httpinvoker.requestor.ResponseProcessor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * scan the given basePackages and register bean of includeAnn-annotated interfaces' implementation that HttpProxyBeanFactory generated
 *
 * @author huangxuyang
 * date 2018/11/1
 */
public class ClassPathHttpApiScanner extends ClassPathBeanDefinitionScanner {
    private static final String HTTP_API_PREFIX = "$HttpApi$";
    private Class<? extends FactoryBean> factoryBean;
    private Class<? extends Annotation> includeAnn;
    private PropertyResolver propertyResolver;
    private Requestor requestor;
    private RequestPreprocessor requestPreprocessor;
    private ResponseProcessor responseProcessor;
    private BeanDefinitionRegistry registry;

    public ClassPathHttpApiScanner(BeanDefinitionRegistry registry, PropertyResolver propertyResolver,
                                   Requestor requestor, RequestPreprocessor requestPreprocessor,
                                   ResponseProcessor responseProcessor) {
        super(registry, false);
        // use DefaultBeanNameGenerator to prevent bean name conflict
        setBeanNameGenerator(new DefaultBeanNameGenerator());
        this.registry = registry;
        this.propertyResolver = propertyResolver;
        this.factoryBean = HttpApiProxyFactoryBean.class;
        this.includeAnn = HttpApi.class;
        addIncludeFilter(new AnnotationTypeFilter(includeAnn));
        this.requestor = requestor;
        this.requestPreprocessor = requestPreprocessor;
        this.responseProcessor = responseProcessor;
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            logger.warn("No " + includeAnn.getSimpleName() + " was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        }
        return beanDefinitions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        // Sometimes, the package scan will be conflicted with MyBatis
        // so the existing is not the expected one, we remove it.
        if (this.registry.containsBeanDefinition(beanName)) {
            // an HttpApi bean exists, we ignore the others.
            if (isHttpApiBean(beanName)) {
                logger.info("an HttpApi bean [" + beanName + "] exists, we ignore the others");
                return false;
            }
            logger.warn("an not HttpApi bean named [" + beanName + "] exists, we remove it, so that we can generate a new bean.");
            registry.removeBeanDefinition(beanName);
        }
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            logger.warn("Skipping " + factoryBean.getSimpleName() + " with name '" + beanName
                    + "' and '" + beanDefinition.getBeanClassName() + "' interface"
                    + ". Bean already defined with the same name!");
            return false;
        }
    }

    private boolean isHttpApiBean(String beanName) {
        if (registry instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
            return beanFactory.containsBean(beanName) && beanFactory.getBean(beanName).toString().startsWith(HTTP_API_PREFIX);
        }
        return false;
    }

    /**
     * registry the bean with FactoryBean
     */
    @Override
    protected void registerBeanDefinition(BeanDefinitionHolder holder, BeanDefinitionRegistry registry) {
        GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
        if (logger.isDebugEnabled()) {
            logger.debug(includeAnn.getSimpleName() + ": Bean with name '" + holder.getBeanName()
                    + "' and '" + definition.getBeanClassName() + "' interface");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Enabling autowire by type for " + factoryBean.getSimpleName() + " with name '" + holder.getBeanName() + "'.");
        }
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        // 需要被代理的接口 the interface
        definition.getPropertyValues().add("interfaceClass", definition.getBeanClassName());
        // 配置项
        definition.getPropertyValues().add("propertyResolver", propertyResolver);
        if (requestor != null) {
            definition.getPropertyValues().add("requestor", requestor);
        }
        if (requestPreprocessor != null) {
            definition.getPropertyValues().add("requestPreprocessor", requestPreprocessor);
        }
        if (responseProcessor != null) {
            definition.getPropertyValues().add("responseProcessor", responseProcessor);
        }
        // 获取bean名，注意：获取 BeanName 要在setBeanClass之前，否则BeanName就会被覆盖
        // caution! we nned to getBeanName first before setBeanClass
        String beanName = holder.getBeanName();
        // 将BeanClass设置成Bean工厂
        definition.setBeanClass(factoryBean);
        // 注册Bean
        registry.registerBeanDefinition(beanName, definition);
    }

}
