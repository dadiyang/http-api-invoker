package com.github.dadiyang.httpinvoker.spring;

import com.github.dadiyang.httpinvoker.annotation.HttpApiScan;
import com.github.dadiyang.httpinvoker.propertyresolver.EnvironmentBasePropertyResolver;
import com.github.dadiyang.httpinvoker.propertyresolver.MultiSourcePropertyResolver;
import com.github.dadiyang.httpinvoker.propertyresolver.PropertiesBasePropertyResolver;
import com.github.dadiyang.httpinvoker.propertyresolver.PropertyResolver;
import com.github.dadiyang.httpinvoker.requestor.RequestPreprocessor;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import com.github.dadiyang.httpinvoker.requestor.ResponseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static com.github.dadiyang.httpinvoker.util.IoUtils.*;

/**
 * Scanning the base packages that {@link HttpApiScan} specified.
 *
 * @author huangxuyang
 * date 2018/10/31
 */
@Component
public class HttpApiConfigurer implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(HttpApiConfigurer.class);
    private static final String CLASSPATH_PRE = "classpath:";
    private static final String FILE_PRE = "file:";
    private ApplicationContext ctx;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        Map<String, Object> beans = ctx.getBeansWithAnnotation(HttpApiScan.class);
        Set<String> basePackages = new HashSet<String>();
        Properties properties = new Properties();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            HttpApiScan ann = entry.getValue().getClass().getAnnotation(HttpApiScan.class);
            if (ann.value().length <= 0 || ann.value()[0].isEmpty()) {
                // add the annotated class' package as a basePackage
                basePackages.add(entry.getValue().getClass().getPackage().getName());
            } else {
                basePackages.addAll(Arrays.asList(ann.value()));
            }
            String[] configPaths = ann.configPaths();
            if (configPaths.length > 0) {
                for (String path : configPaths) {
                    if (path == null || path.isEmpty()) {
                        continue;
                    }
                    try {
                        Properties p = getProperties(path);
                        properties.putAll(p);
                    } catch (IOException e) {
                        throw new IllegalStateException("read config error: " + path, e);
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("HttpApiScan packages: " + basePackages);
        }
        Requestor requestor = null;
        try {
            requestor = ctx.getBean(Requestor.class);
        } catch (Exception e) {
            logger.debug("Requestor bean does not exist: " + e.getMessage());
        }
        RequestPreprocessor requestPreprocessor = null;
        try {
            requestPreprocessor = ctx.getBean(RequestPreprocessor.class);
        } catch (Exception e) {
            logger.debug("RequestPreprocessor bean does not exist" + e.getMessage());
        }
        ResponseProcessor responseProcessor = null;
        try {
            responseProcessor = ctx.getBean(ResponseProcessor.class);
        } catch (Exception e) {
            logger.debug("ResponseProcessor bean does not exist" + e.getMessage());
        }
        PropertyResolver resolver;
        if (properties.size() > 0) {
            MultiSourcePropertyResolver multi = new MultiSourcePropertyResolver();
            // use properties both from config files and environment
            multi.addPropertyResolver(new PropertiesBasePropertyResolver(properties));
            multi.addPropertyResolver(new EnvironmentBasePropertyResolver(ctx.getEnvironment()));
            resolver = multi;
        } else {
            // use properties from environment
            resolver = new EnvironmentBasePropertyResolver(ctx.getEnvironment());
        }
        ClassPathHttpApiScanner scanner = new ClassPathHttpApiScanner(beanDefinitionRegistry, resolver, requestor, requestPreprocessor, responseProcessor);
        scanner.doScan(basePackages.toArray(new String[]{}));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws
            BeansException {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
