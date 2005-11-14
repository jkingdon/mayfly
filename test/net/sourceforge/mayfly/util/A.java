package net.sourceforge.mayfly.util;

import junit.framework.*;
import net.sourceforge.mayfly.*;

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

    public static void assertNotEquals(Object expected, Object actual) {
        boolean equal;

        try {
            assertEquals(expected, actual);
            equal = true;
        } catch (AssertionFailedError err) {
            equal = false;
        }

        if (equal) {
            throw new AssertionFailedError("expected " + String.valueOf(expected) + " to not be equal to " + String.valueOf(actual));
        }
    }

    public static void assertMayflyException(String expectedExceptionMessage, ExceptionBlock block) {
        try {
            block.execute();
        } catch (MayflyException ex) {
            assertEquals(expectedExceptionMessage, ex.getMessage());
        }
    }
}
