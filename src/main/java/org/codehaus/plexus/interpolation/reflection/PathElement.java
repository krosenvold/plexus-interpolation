package org.codehaus.plexus.interpolation.reflection;

/*
 * Copyright 2014 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.InvocationTargetException;
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

    public static PathElement UNRESOLVABLE = new PathElement( null, null ){
        @Override
        public Object evaluate( Object root )
            throws InvocationTargetException, IllegalAccessException
        {
            return null;
        }
    };

    public static PathElement createPathElement( Method getter, PathElement next )
    {
        return new PathElement( getter, next );
    }

    public Object evaluate( Object root )
        throws InvocationTargetException, IllegalAccessException
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
