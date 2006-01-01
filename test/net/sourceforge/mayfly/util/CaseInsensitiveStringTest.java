package net.sourceforge.mayfly.util;

import junit.framework.*;

public class CaseInsensitiveStringTest extends TestCase {

    public void testBasics() throws Exception {
        assertAllEqual(new Object[] { 
            new CaseInsensitiveString("Foo"), 
            new CaseInsensitiveString("Foo"), 
            new CaseInsensitiveString("foo"),
            new CaseInsensitiveString("FOO") 
        });

        assertIsNotEqual(new CaseInsensitiveString("Foo"), new CaseInsensitiveString("Food"));
        assertIsNotEqual(new CaseInsensitiveString("Foo"), "Foo");
        assertIsNotEqual(new CaseInsensitiveString("Foo"), new Long(5));
    }

    /**
     * @internal 
     * 
     * Here is our equals/hashCode testing framework.  Is there really
     * not just one to download?  This wheel gets reinvented so often.
     * The one in {@link EqualsHashCodeTestCase} is seriously broken -
     * it often gets confused about which equals method it is testing
     * (e.g. the one from Object or the one under test) and similar
     * problems.
     */
    
    private void assertAllEqual(Object[] objects) {
        /**
         * The point of checking each pair is to make sure that equals is
         * transitive per the contract of {@link Object#equals(java.lang.Object)}.
         */
        for (int i = 0; i < objects.length; i++) {
            assertFalse(objects[i].equals(null));
            for (int j = 0; j < objects.length; j++) {
                assertIsEqual(objects[i], objects[j]);
            }
        }
    }

    private void assertIsEqual(Object one, Object two) {
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
        assertEquals(one.hashCode(), two.hashCode());
    }

    private void assertIsNotEqual(Object one, Object two) {
        assertReflexiveAndNull(one);
        assertReflexiveAndNull(two);
        assertFalse(one.equals(two));
        assertFalse(two.equals(one));
    }

    private void assertReflexiveAndNull(Object object) {
        assertTrue(object.equals(object));
        assertFalse(object.equals(null));
    }

}
