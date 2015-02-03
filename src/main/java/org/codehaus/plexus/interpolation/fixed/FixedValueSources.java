/*
    Copyright 2015 the original author or authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/


package org.codehaus.plexus.interpolation.fixed;

import org.codehaus.plexus.interpolation.ValueSource;

import java.util.Iterator;
import java.util.List;

public class FixedValueSources
{
    public static FixedValueSource fromValueSource( final ValueSource valueSource ){
        return new FixedValueSource()
        {
            public Object getValue( String expression, InterpolationState interpolationState )
            {
                Object result = valueSource.getValue( expression );
                List feedback = valueSource.getFeedback();
                if (feedback != null)
                {
                    Iterator iterator = feedback.iterator();
                    while (iterator.hasNext())
                    {
                        String msg = (String) iterator.next();
                        Throwable t = null;
                        if (iterator.hasNext()){
                            t = (Throwable) iterator.next();
                        }
                        interpolationState.addFeedback( msg, t );
                    }
                    feedback.clear();
                }
                return result;
            }
        };
    }
}
