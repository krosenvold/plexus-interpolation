package org.codehaus.plexus.interpolation.reflection;

import java.lang.reflect.Method;

public class PathElement2
{
    private final Method getter;
    private final PathElement2 next;

    private PathElement2( Method getter, PathElement2 next )
    {
        this.getter = getter;
        this.next = next;
    }

    public static PathElement2 createPathElement2( Method getter, PathElement2 next )
    {
        return new PathElement2( getter, next );
    }


    public static PathElement2 createFinalElement( Method getter)    {

        return new PathElement2( getter, null );
    }


    public Object evaluate( Object root )
        throws Exception
    {

        final Object invoke = getter.invoke( root );
        if (next != null) return next.evaluate( invoke );
        else return invoke;
    }
}
