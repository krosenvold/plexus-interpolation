package org.codehaus.plexus.interpolation.reflection;

import java.lang.reflect.Method;

public class PathElement
{
    private final Method getter;

    private final PathElement next;

    private PathElement( Method getter, PathElement next )
    {
        this.getter = getter;
        this.next = next;
    }

    public static PathElement createPathElement( Method getter, PathElement next )
    {
        return new PathElement( getter, next );
    }

    public Object evaluate( Object root )
        throws Exception
    {

        final Object invoke = getter.invoke( root );
        if ( next != null )
        {
            return next.evaluate( invoke );
        }
        else
        {
            return invoke;
        }
    }
}
