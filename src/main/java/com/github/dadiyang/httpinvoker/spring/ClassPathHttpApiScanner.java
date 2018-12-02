package com.github.dadiyang.httpinvoker.spring;

import com.github.dadiyang.httpinvoker.annotation.HttpApi;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

/**
 * scan the given basePackages and register bean of includeAnn-annotated interfaces' implementation that HttpProxyBeanFactory generated
 *
 * @author huangxuyang
 * @date 2018/11/1
 */
public class ClassPathHttpApiScanner extends ClassPathBeanDefinitionScanner {
    private Class<? extends FactoryBean> factoryBean;
    private Class<? extends Annotation> includeAnn;
    private Properties properties;
    private Requestor requestor;

    public ClassPathHttpApiScanner(BeanDefinitionRegistry registry, Properties properties, Requestor requestor) {
        super(registry, false);
        this.factoryBean = HttpApiProxyFactoryBean.class;
        this.includeAnn = HttpApi.class;
        addIncludeFilter(new AnnotationTypeFilter(includeAnn));
        this.properties = properties;
        this.requestor = requestor;
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
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            logger.warn("Skipping " + factoryBean.getSimpleName() + " with name '" + beanName
                    + "' and '" + beanDefinition.getBeanClassName() + "' mapperInterface"
                    + ". Bean already defined with the same name!");
            return false;
        }
    }

    /**
     * registry the bean with FactoryBean
     */
    @Override
    protected void registerBeanDefinition(BeanDefinitionHolder holder, BeanDefinitionRegistry registry) {
        GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + includeAnn.getSimpleName() + "Bean with name '" + holder.getBeanName()
                    + "' and '" + definition.getBeanClassName() + "' interface");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Enabling autowire by type for " + factoryBean.getSimpleName() + " with name '" + holder.getBeanName() + "'.");
        }
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        // 需要被代理的接口 the interface
        definition.getPropertyValues().add("interfaceClass", definition.getBeanClassName());
        // 配置项
        definition.getPropertyValues().add("properties", properties);
        if (requestor != null) {
            definition.getPropertyValues().add("requestor", requestor);
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
