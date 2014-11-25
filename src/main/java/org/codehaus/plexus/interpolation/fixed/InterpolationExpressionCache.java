package org.codehaus.plexus.interpolation.fixed;

import org.codehaus.plexus.interpolation.reflection.MethodMap;
import org.codehaus.plexus.interpolation.reflection.PathElement;
import org.codehaus.plexus.interpolation.reflection.ReflectionPathBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread safe cache that stores class<->expression resolutions.
 */
public class InterpolationExpressionCache
{
    private final Class clazz;

    private final ConcurrentHashMap<String, PathElement> paths = new ConcurrentHashMap<String, PathElement>();

    /**
     * Create an expression cache that understands root objects of a given class.
     * @param clazz The class for which this cache will
     */
    public InterpolationExpressionCache( Class clazz )
    {
        this.clazz = clazz;
    }

    public PathElement getPath( String expression )
        throws IllegalAccessException, MethodMap.AmbiguousException, InvocationTargetException
    {

        PathElement pathElement = paths.get( expression );
        if ( pathElement == null )
        {
            pathElement = ReflectionPathBuilder.createPath( expression, false, clazz );
            paths.putIfAbsent( expression, pathElement );
        }
        return pathElement;
    }
}
