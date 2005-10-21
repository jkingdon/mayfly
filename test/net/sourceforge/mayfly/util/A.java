package net.sourceforge.mayfly.util;

import junit.framework.*;

public class A {
    public static void assertEquals(Object expected, Object actual) {

        try {
            Assert.assertEquals(expected, actual);
        } catch (AssertionFailedError err) {
            if (!String.valueOf(expected).equals(String.valueOf(actual))) {
                Assert.assertEquals(String.valueOf(expected), String.valueOf(actual));
            } else {
                throw err;
            }
        }
    }
}
