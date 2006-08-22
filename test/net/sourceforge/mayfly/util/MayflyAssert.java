package net.sourceforge.mayfly.util;

import junit.framework.Assert;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.ldbc.what.SingleColumn;

public class MayflyAssert {

    public static void assertColumn(String expectedTableOrAlias, String expectedColumn, SingleColumn actual) {
        Assert.assertEquals(expectedTableOrAlias, actual.tableOrAlias());
        Assert.assertEquals(expectedColumn, actual.columnName());
    }

    /** 
     * How does this compare to
     * {@link ObjectAssert#assertInstanceOf(Class, Object)}
     */
    public static void assertInstanceOf(Class expectedClass, Object actualObject) {
        Assert.assertTrue(
            "Expected " + expectedClass.getName() + " but got " + actualObject.getClass().getName(),
            expectedClass.isAssignableFrom(actualObject.getClass())
        );
    }

}
