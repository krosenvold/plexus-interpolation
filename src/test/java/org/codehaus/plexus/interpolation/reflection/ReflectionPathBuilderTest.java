package org.codehaus.plexus.interpolation.reflection;

import junit.framework.TestCase;

@SuppressWarnings( "UnusedDeclaration" )
public class ReflectionPathBuilderTest
    extends TestCase
{

    public static class TC1 {
        public TC2 getAz(){
            return new TC2();
        }
    }

    public static class InnerClassParent {
        public InnerClassChild getChild(){
            return new InnerClassChild();
        }
    }

    public static class InnerClassParent2 extends InnerClassParent {
        public InnerClassChild getChild(){
            return new InnerClassChild(){
                @Override
                public String getValue()
                {
                    return "fzz";
                }
            };
        }
    }

    public static class InnerClassChild {
        public String getValue(){
            return "aValue";
        }
    }

    public static class TC1B extends TC1 {
        public TC2 getAz(){
            return new TC2(){
                @Override
                public String getBar()
                {
                    return "Zap";
                }
            };
        }
    }

    public static class TC1C extends TC1 {
        public TC2 getAz(){
            return new TC2(){
                @Override
                public String getBar()
                {
                    return null;
                }
            };
        }
    }

    public static class TC2 {
        public String getBar(){
            return "Baz";
        }
    }

    public void testCreatePath()
        throws Exception
    {
        PathElement path = ReflectionPathBuilder.createPath( "az.bar", false, TC1.class );
        assertEquals( "Baz",  path.evaluate( new TC1() ));
        assertEquals( "Zap", path.evaluate( new TC1B() ) );
        assertEquals( null, path.evaluate( new TC1C() ) );
    }

    public void testMissingExpr()
        throws Exception
    {
        PathElement path = ReflectionPathBuilder.createPath( "az.baz", false, TC1.class );
        assertSame( PathElement.UNRESOLVABLE, path );
    }

    public void testPathcalculatedFromInnerClass()
        throws Exception
    {
        PathElement path = ReflectionPathBuilder.createPath( "child.value", false, InnerClassParent2.class);
        assertNotNull( path );
        assertEquals( "fzz", path.evaluate( new InnerClassParent2() ));
        assertNotNull( path );
        assertEquals( "aValue", path.evaluate( new InnerClassParent() ) );
    }

    public void testExpressionEvaluatesToNull()
        throws Exception
    {
        PathElement path = ReflectionPathBuilder.createPath( "az.bar", false, TC1C.class );
        assertNotNull( path );
        assertNull( path.evaluate( new TC1C() ));
        assertNotNull( path );
        assertEquals( "Baz", path.evaluate( new TC1() ));
    }

}