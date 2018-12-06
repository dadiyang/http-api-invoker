package com.github.dadiyang.httpinvoker.spring;

import com.github.dadiyang.httpinvoker.annotation.HttpApiScan;
import com.github.dadiyang.httpinvoker.requestor.DefaultHttpRequestor;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
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
    private ApplicationContext ctx;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        Map<String, Object> beans = ctx.getBeansWithAnnotation(HttpApiScan.class);
        Set<String> basePackages = new HashSet<>();
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
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
                    if (path.startsWith(CLASSPATH_PRE)) {
                        path = path.replace(CLASSPATH_PRE, "");
                        Properties p = new Properties();
                        try {
                            p.load(getClass().getClassLoader().getResourceAsStream(path));
                        } catch (IOException e) {
                            throw new IllegalStateException("read config error: " + path, e);
                        }
                        properties.putAll(p);
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("HttpApiScan packages: " + basePackages);
        }
        Requestor requestor;
        try {
            requestor = ctx.getBean(Requestor.class);
        } catch (Exception ignored) {
            logger.debug("Requestor bean is not exist, use the default one");
            requestor = new DefaultHttpRequestor();
        }
        ClassPathHttpApiScanner scanner = new ClassPathHttpApiScanner(beanDefinitionRegistry, properties, requestor);
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
