package org.codehaus.plexus.interpolation.fixed;

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

import org.codehaus.plexus.interpolation.util.ValueSourceUtils;

import java.util.List;

/**
 * {@link org.codehaus.plexus.interpolation.fixed.FixedValueSource} implementation which simply wraps another
 * value source, and trims any of a set of possible expression prefixes before delegating the
 * modified expression to be resolved by the real value source.
 *
 * @author jdcasey
 * @author krosenvold
 * @version $Id$
 */
public class PrefixedValueSource
    implements FixedValueSource
{

    private final FixedValueSource valueSource;

    private final String[] possiblePrefixes;

    private final boolean allowUnprefixedExpressions;

    public static PrefixedValueSource prefix(FixedValueSource source){
        return new PrefixedValueSource( source, false, null ) ;
    }

    /*
     * Add prefixes that will be trimmed from expression before evaluating.
     * @param prefixes the prefixes that will be removed if present
     */
    public PrefixedValueSource with(List<String> prefixes){
        return new PrefixedValueSource( valueSource,allowUnprefixedExpressions, prefixes.toArray(new String[prefixes.size()]) );
    }

    /*
     * Add prefixes that will be trimmed from expression before evaluating.
     * @param prefixes the prefixes that will be removed if present
     */
    public PrefixedValueSource with(String... prefixes){
        return new PrefixedValueSource( valueSource,allowUnprefixedExpressions, prefixes);
    }

    /*
     * Permit the expressions to have unprefixed values
     */

    public PrefixedValueSource allowUnprefixed(){
        return new PrefixedValueSource( valueSource,true, possiblePrefixes );
    }

    /**
     * Wrap the given value source, but first trim one of the given prefixes from any
     * expressions before they are passed along for resolution. If an expression
     * doesn't start with the given prefix and the allowUnprefixedExpressions flag
     * is set to true, simply pass the expression through to the nested value source
     * unchanged. If this flag is false, only allow resolution of those expressions
     * that start with the specified prefix.
     *
     * @param valueSource                The {@link org.codehaus.plexus.interpolation.ValueSource} to wrap.
     * @param possiblePrefixes           The List of expression prefixes to trim.
     * @param allowUnprefixedExpressions Flag telling the wrapper whether to
     *                                   continue resolving expressions that don't start with one of the prefixes it tracks.
     */
    private PrefixedValueSource( FixedValueSource valueSource,boolean allowUnprefixedExpressions, String... possiblePrefixes )
    {
        this.valueSource = valueSource;
        this.possiblePrefixes = possiblePrefixes;
        this.allowUnprefixedExpressions = allowUnprefixedExpressions;
    }

    public Object getValue( String expression, InterpolationState interpolationState )
    {
        expression = ValueSourceUtils.trimPrefix( expression, possiblePrefixes, allowUnprefixedExpressions );

        if ( expression == null )
        {
            return null;
        }

        return valueSource.getValue( expression, interpolationState );
    }
}
