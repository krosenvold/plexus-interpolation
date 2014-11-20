package org.codehaus.plexus.interpolation.reflection;

import java.lang.reflect.Method;

public class PathElement
{
    private final Method getter;
//    private final PathElement next;

    public PathElement( Method getter )
    {
        this.getter = getter;
    }

    public Object evaluate( Object root )
        throws Exception
    {
        return getter.invoke( root );
    }
}
