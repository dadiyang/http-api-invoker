package com.github.dadiyang.httpinvoker.propertyresolver;

import java.util.Properties;

/**
 * A PropertyResolver base on a Properties object.
 *
 * @author dadiyang
 * @since 1.0.9
 */
public class PropertiesBasePropertyResolver implements PropertyResolver {
    private Properties properties;

    public PropertiesBasePropertyResolver(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PropertiesBasePropertyResolver that = (PropertiesBasePropertyResolver) o;
        return properties != null ? properties.equals(that.properties) : that.properties == null;
    }

    @Override
    public int hashCode() {
        return properties != null ? properties.hashCode() : 0;
    }
}
