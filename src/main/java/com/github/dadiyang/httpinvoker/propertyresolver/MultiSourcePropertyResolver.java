package com.github.dadiyang.httpinvoker.propertyresolver;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A PropertyResolver which includes a set of PropertyResolvers
 *
 * @author huangxuyang
 * @since 1.0.9
 */
public class MultiSourcePropertyResolver implements PropertyResolver {
    private Set<PropertyResolver> resolvers;

    /**
     * Construct by a given resolvers list.
     * <p>
     * Note that a new HashSet will be use,
     * so if you want to add a new Resolver, call {@link #addPropertyResolver} please
     *
     * @param resolvers resolvers list
     * @throws IllegalArgumentException if param resolvers is null
     */
    public MultiSourcePropertyResolver(Set<PropertyResolver> resolvers) {
        if (resolvers == null) {
            throw new IllegalArgumentException("resolvers must not be null");
        }
        this.resolvers = new LinkedHashSet<PropertyResolver>(resolvers);
    }

    public MultiSourcePropertyResolver() {
        resolvers = new LinkedHashSet<PropertyResolver>();
    }

    public Set<PropertyResolver> getResolvers() {
        return Collections.unmodifiableSet(resolvers);
    }

    /**
     * Add a new PropertyResolver;
     *
     * @param resolver a propertyResolver
     */
    public void addPropertyResolver(PropertyResolver resolver) {
        this.resolvers.add(resolver);
    }

    @Override
    public boolean containsProperty(String key) {
        for (PropertyResolver resolver : resolvers) {
            if (resolver.containsProperty(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getProperty(String key) {
        for (PropertyResolver resolver : resolvers) {
            if (resolver.containsProperty(key)) {
                return resolver.getProperty(key);
            }
        }
        return null;
    }
}
