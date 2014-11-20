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

import org.codehaus.plexus.interpolation.util.StringUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

/**
 * Builds a path that will resolve a given expression
 * from a root object. The path is dependant only
 * on the *type* of the object
 *
 * @author Kristian Rosenvold
 */
public class ReflectionPathBuilder
{
    private static final Class<?>[] CLASS_ARGS = new Class[0];

    private static final Object[] OBJECT_ARGS = new Object[0];

    /**
     * Use a WeakHashMap here, so the keys (Class objects) can be garbage collected.
     * This approach prevents permgen space overflows due to retention of discarded
     * classloaders.
     */
    private static final Map<Class<?>, WeakReference<ClassMap>> classMaps = new WeakHashMap<Class<?>, WeakReference<ClassMap>>();

    private ReflectionPathBuilder()
    {
    }

    public static FirstResult createPath( String expression, boolean trimRootToken, Object root )
        throws Exception
    {

        // if the root token refers to the supplied root object parameter, remove it.
        if ( trimRootToken )
        {
            expression = expression.substring( expression.indexOf( '.' ) + 1 );
        }


        List<Method> result = new ArrayList<Method>(  );
        final Object o = evaluateZZ( expression, root, result );
        if (o == null) return null; // Nothing. Maybe use some kind fo identityPathElement or other bollocks
        PathElement2 pe2 = null;
        for (int i = result.size() - 1; i>= 0; i--){
            pe2 = PathElement2.createPathElement2( result.get( i ), pe2 );
        }
        return new FirstResult( pe2, o );
    }

    private static Object evaluateZZ( String expression, Object root, List<Method> result )
        throws Exception
    {
        Object value = root;

        StringTokenizer parser = new StringTokenizer( expression, "." );

        while ( parser.hasMoreTokens() )
        {
            String token = parser.nextToken();

            if ( value == null )
            {
                return null;
            }

            Method method = findMethod( value, token );

            if ( method == null )
            {
                return null;
            }

            result.add(  method);
            value = method.invoke( value, OBJECT_ARGS );
        }

        return value;
    }

    private static PathElement2 evaluate( String expression, Object root, boolean trimRootToken )
        throws Exception
    {
        // if the root token refers to the supplied root object parameter, remove it.
        if ( trimRootToken )
        {
            expression = expression.substring( expression.indexOf( '.' ) + 1 );
        }

        Object value = root;

        // ----------------------------------------------------------------------
        // Walk the dots and retrieve the ultimate value desired from the
        // MavenProject instance.
        // ----------------------------------------------------------------------

        StringTokenizer parser = new StringTokenizer( expression, "." );

        return evaluate(  root, parser );
    }

    private static PathElement2 evaluate( Object currentObject, StringTokenizer parser )
        throws Exception
    {
        String token = parser.nextToken();

        if ( currentObject == null )
        {
            return null;
        }

        Method method = findMethod( currentObject, token );

        if ( method == null )
        {
            return null;
        }

        Object value = method.invoke( currentObject, OBJECT_ARGS );
        if ( parser.hasMoreTokens() )
        {
            return PathElement2.createPathElement2( method, evaluate( value, parser ) );
        }
        else
        {
            return PathElement2.createFinalElement( method );
        }
    }


    private static Method findMethod( Object currentObject, String token )
        throws MethodMap.AmbiguousException
    {
        ClassMap classMap = getClassMap( currentObject.getClass() );

        String methodBase = StringUtils.capitalizeFirstLetter( token );

        String methodName = "get" + methodBase;

        Method method = classMap.findMethod( methodName, CLASS_ARGS );

        if ( method == null )
        {
            // perhaps this is a boolean property??
            methodName = "is" + methodBase;

            method = classMap.findMethod( methodName, CLASS_ARGS );
        }
        return method;
    }

    private static ClassMap getClassMap( Class<?> clazz )
    {
        WeakReference<ClassMap> ref = classMaps.get( clazz);

        ClassMap classMap;

        if ( ref == null || (classMap = ref.get()) == null )
        {
            classMap = new ClassMap( clazz );

            classMaps.put( clazz, new WeakReference<ClassMap>(classMap) );
        }

        return classMap;
    }
}
