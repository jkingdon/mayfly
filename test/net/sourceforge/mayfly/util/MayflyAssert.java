package net.sourceforge.mayfly.util;

import junit.framework.Assert;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
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

    public static void assertInteger(int expected, Expression actual) {
        IntegerLiteral ten = (IntegerLiteral) actual;
        Assert.assertEquals(expected, ten.value);
    }

    public static void assertString(String expected, Expression actual) {
        QuotedString string = (QuotedString) actual;
        Assert.assertEquals(expected, string.stringWithoutQuotes());
        
    }

    public static void assertColumn(String expectedName, Expression expression) {
        SingleColumn y = (SingleColumn) expression;
        Assert.assertEquals(expectedName, y.columnName());
    }

}
