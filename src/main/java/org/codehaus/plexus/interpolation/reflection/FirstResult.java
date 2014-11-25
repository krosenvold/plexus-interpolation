package org.codehaus.plexus.interpolation.reflection;

public class FirstResult
{

    private final PathElement path;
    private final Object value;

    public FirstResult( PathElement path, Object value )
    {
        this.path = path;
        this.value = value;
    }

    public PathElement getPath()
    {
        return path;
    }

    public Object getValue()
    {
        return value;
    }
}
