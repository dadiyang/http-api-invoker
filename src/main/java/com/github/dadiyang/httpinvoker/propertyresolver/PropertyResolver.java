package com.github.dadiyang.httpinvoker.propertyresolver;

/**
 * Interface for resolving properties.
 *
 * @author dadiyang
 * @since 1.0.9
 */
public interface PropertyResolver {
    /**
     * Check whether the given property key is available.
     *
     * @param key the property name to resolve
     * @return whether the given property key is available.
     */
    boolean containsProperty(String key);

    /**
     * Return the property value associated with the given key,
     * or {@code null} if the key cannot be resolved.
     *
     * @param key the property name to resolve
     * @return the property value associated with the given key
     */
    String getProperty(String key);
}
