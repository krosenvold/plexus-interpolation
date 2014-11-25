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
        FirstResult path = ReflectionPathBuilder.createPath( "az.bar", false, new TC1() );
        assertEquals( "Baz", path.getValue());
        PathElement path1 = path.getPath();
        assertEquals( "Zap", path1.evaluate( new TC1B() ) );
        assertEquals( null, path1.evaluate( new TC1C() ) );
    }

    public void testMissingExpr()
        throws Exception
    {
        FirstResult path = ReflectionPathBuilder.createPath( "az.baz", false, new TC1() );
        assertNull( path );
    }

    public void testPathcalculatedFromInnerClass()
        throws Exception
    {
        FirstResult path = ReflectionPathBuilder.createPath( "child.value", false, new InnerClassParent2());
        assertNotNull( path );
        assertEquals( "fzz", path.getValue());
        PathElement path1 = path.getPath();
        assertNotNull( path1 );
        assertEquals( "aValue", path1.evaluate( new InnerClassParent() ) );
    }

    public void testExpressionEvaluatesToNull()
        throws Exception
    {
        FirstResult path = ReflectionPathBuilder.createPath( "az.bar", false, new TC1C() );
        assertNotNull( path );
        assertNull( path.getValue());
        PathElement path1 = path.getPath();
        assertNotNull( path1 );
        assertEquals( "Baz", path1.evaluate( new TC1() ));
    }

}