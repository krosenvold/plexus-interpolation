package org.codehaus.plexus.interpolation.fixed;

import java.util.concurrent.ConcurrentHashMap;

public class InterpolationExpressionCaches
{
    private final ConcurrentHashMap<Class, InterpolationExpressionCache> caches = new ConcurrentHashMap<Class, InterpolationExpressionCache>(  );

    public InterpolationExpressionCache getCache(Class clazz){
        InterpolationExpressionCache cache = caches.get( clazz );
        if (cache == null){
            cache = new InterpolationExpressionCache( clazz );
            caches.putIfAbsent( clazz, cache );
        }
        return cache;
    }
}
