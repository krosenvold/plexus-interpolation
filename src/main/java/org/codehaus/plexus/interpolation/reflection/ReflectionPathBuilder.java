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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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

    private ReflectionPathBuilder()
    {
    }

    public static PathElement createPath( String expression, boolean trimRootToken, Class rootClass )
        throws IllegalAccessException, MethodMap.AmbiguousException, InvocationTargetException
    {
        if ( trimRootToken )
        {
            expression = expression.substring( expression.indexOf( '.' ) + 1 );
        }

        List<Method> result = evaluate( expression, rootClass );
        if ( result == null || result.size() == 0 )
        {
            return PathElement.UNRESOLVABLE;
        }
        PathElement pathElement = null;
        for ( int i = result.size() - 1; i >= 0; i-- )
        {
            pathElement = PathElement.createPathElement( result.get( i ), pathElement );
        }
        return pathElement;
    }

    private static List<Method> evaluate( String expression, Class rootClass )
        throws InvocationTargetException, IllegalAccessException, MethodMap.AmbiguousException
    {
        List<Method> result = new ArrayList<Method>();

        StringTokenizer parser = new StringTokenizer( expression, "." );

        Class currentType = rootClass;

        while ( parser.hasMoreTokens() )
        {
            String token = parser.nextToken();

            if ( currentType == null )
            {
                return null;
            }

            Method method = findMethod( currentType, token );

            if ( method == null )
            {
                return null;
            }

            result.add( method );

            currentType = method.getReturnType();
        }
        return result;
    }

    private static Method findMethod( Class currentObjectClass, String token )
        throws MethodMap.AmbiguousException
    {
        String methodBase = StringUtils.capitalizeFirstLetter( token );

        return populateMethodCache( currentObjectClass, methodBase );

    }

    private static Method populateMethodCache( Class clazz, String methodBase )
        throws MethodMap.AmbiguousException
    {
        MethodMap methodMap = new MethodMap();

        String getter = "get" + methodBase;
        String isMethod = "is" + methodBase;

        Set<String> desiredMethods = new HashSet<String>();
        desiredMethods.add( getter );
        desiredMethods.add( isMethod );
        Method[] methods = getAccessibleMethods( clazz, desiredMethods );

        for ( Method method : methods )
        {
            /*
             *  now get the 'public method', the method declared by a
             *  public interface or class. (because the actual implementing
             *  class may be a facade...
             */

            Method publicMethod = ClassMap.getPublicMethod( method );

            /*
             *  it is entirely possible that there is no public method for
             *  the methods of this class (i.e. in the facade, a method
             *  that isn't on any of the interfaces or superclass
             *  in which case, ignore it.  Otherwise, map and cache
             */

            if ( publicMethod != null )
            {
                methodMap.add( publicMethod );
            }
        }

        final Method method = methodMap.find( getter, CLASS_ARGS );
        if ( method != null )
        {
            return method;
        }
        return methodMap.find( isMethod, CLASS_ARGS );
    }

    /**
     * Retrieves public methods for a class. In case the class is not
     * public, retrieves methods with same signature as its public methods
     * from public superclasses and interfaces (if they exist). Basically
     * upcasts every method to the nearest acccessible method.
     */
    private static Method[] getAccessibleMethods( Class<?> clazz, Set<String> desiredMethods )
    {
        List<Method> toUse = new ArrayList<Method>();
        for ( Method method : clazz.getMethods() )
        {
            if ( desiredMethods.contains( method.getName() ) )
            {
                toUse.add( findBestMatchingMethod( method ));
            }
        }
        Method[] methods = toUse.toArray( new Method[toUse.size()] );


        /*
         *  Short circuit for the (hopefully) majority of cases where the
         *  clazz is public
         */

        if ( Modifier.isPublic( clazz.getModifiers() ) )
        {
            return methods;
        }

        /*
         *  No luck - the class is not public, so we're going the longer way.
         */

        ClassMap.MethodInfo[] methodInfos = new ClassMap.MethodInfo[methods.length];

        for ( int i = methods.length; i-- > 0; )
        {
            methodInfos[i] = new ClassMap.MethodInfo( methods[i] );
        }

        int upcastCount = ClassMap.getAccessibleMethods( clazz, methodInfos, 0 );

        /*
         *  Reallocate array in case some method had no accessible counterpart.
         */

        if ( upcastCount < methods.length )
        {
            methods = new Method[upcastCount];
        }

        int j = 0;
        for ( ClassMap.MethodInfo methodInfo : methodInfos )
        {
            if ( methodInfo.upcast )
            {
                methods[j++] = methodInfo.method;
            }
        }
        return methods;
    }

    @SuppressWarnings( "unchecked" )
    private static Method findBestMatchingMethod( Method method )
    {
        Method best = method;
        Class parent = method.getDeclaringClass().getSuperclass();
        while ( parent != null )
        {
            try
            {
                best = parent.getMethod( method.getName(), method.getParameterTypes() );
            }
            catch ( NoSuchMethodException nonebetter )
            {
                return best;
            }
            parent = parent.getSuperclass();
        }
        return best;
    }


}
