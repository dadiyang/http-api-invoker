package com.github.dadiyang.httpinvoker.propertyresolver;

import org.springframework.core.env.Environment;

/**
 * A PropertyResolver base on Spring Environment object.
 *
 * @author dadiyang
 * @since 1.0.9
 */
public class EnvironmentBasePropertyResolver implements PropertyResolver {
    private Environment environment;

    public EnvironmentBasePropertyResolver(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }

    @Override
    public String getProperty(String key) {
        return environment.getProperty(key);
    }
}
