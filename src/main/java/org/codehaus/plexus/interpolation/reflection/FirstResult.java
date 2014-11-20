package org.codehaus.plexus.interpolation.reflection;

public class FirstResult
{

    private final PathElement2 path;
    private final Object value;

    public FirstResult( PathElement2 path, Object value )
    {
        this.path = path;
        this.value = value;
    }

    public PathElement2 getPath()
    {
        return path;
    }

    public Object getValue()
    {
        return value;
    }
}
