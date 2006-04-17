package net.sourceforge.mayfly.util;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ReflectiveEqualsTest extends TestCase {
    
    /* 
       Eventually intended to replace the need to inherit from
       ValueObject (at least when such inheritance is being
       done so the tests can assertEquals on such objects).
     */
    public void testSame() throws Exception {
        Foo foo = new Foo(5);
        assertReflectiveEquals(foo, foo);
    }

    public void testEqual() throws Exception {
        assertReflectiveEquals(new Foo(5), new Foo(5));
    }

    public void testNotEqual() throws Exception {
        try {
            assertReflectiveEquals(new Foo(5), new Foo(8));
            throw new RuntimeException("should have failed");
        }
        catch (AssertionFailedError e) {
            assertEquals(
                "expected ReflectiveEqualsTest.Foo[x=5] but was ReflectiveEqualsTest.Foo[x=8]", 
                e.getMessage());
        }
    }
    
    static class Foo {
        int x;

        public Foo(int x) {
            this.x = x;
        }
    }

    public void xtestNestedEqual() throws Exception {
        // requires the patch, and passing in recurseReflectively
        assertReflectiveEquals(new Bar(5), new Bar(5));
    }
    
    public void xtestNestedNotEqual() throws Exception {
        try {
            assertReflectiveEquals(new Bar(5), new Bar(8));
            throw new RuntimeException("should have failed");
        }
        catch (AssertionFailedError e) {
            // needs the analogue to the patch for the ReflectionToStringBuilder
            assertEquals(
                "expected ReflectiveEqualsTest.Bar[foo=ReflectiveEqualsTest.Foo[x=5]]" +
                " but was " +
                "ReflectiveEqualsTest.Bar[foo=ReflectiveEqualsTest.Foo[x=8]]", 
                e.getMessage());
        }
    }
    
    static class Bar {
        Foo foo;
        
        public Bar(int x) {
            this.foo = new Foo(x);
        }
    }

    private void assertReflectiveEquals(Object expected, Object actual) {
        assertTrue(
            "expected " + toString(expected) + " but was " + toString(actual),
            EqualsBuilder.reflectionEquals(expected, actual)
            );
    }
    
    private static String toString(Object object) {
        /*
            this.setContentStart("[");
            this.setFieldSeparator("\n  ");
            this.setFieldSeparatorAtStart(true);
            this.setContentEnd("\n]");
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);

         */
        return ReflectionToStringBuilder.toString(object, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
