package net.sourceforge.mayfly.util;

import junit.framework.Assert;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.parser.Location;

public class MayflyAssert {

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
        assertColumn(null, expectedName, expression);
    }

    public static void assertColumn(String expectedAlias, String expectedName, 
        Expression expression) {
        SingleColumn y = (SingleColumn) expression;
        Assert.assertEquals(expectedAlias, y.tableOrAlias());
        Assert.assertEquals(expectedName, y.columnName());
    }

    public static void assertColumn(String expectedColumn, int expectedValue, 
        ResultRow row, int index) {
        assertColumn(expectedColumn, row.expression(index));
        assertLong(expectedValue, row.cell(index));
    }

    public static void assertLocation(
        int expectedStartColumn, int expectedEndColumn, Location location) {
        Assert.assertEquals(expectedStartColumn, location.startColumn);
        Assert.assertEquals(expectedEndColumn, location.endColumn);
        Assert.assertEquals(1, location.startLineNumber);
        Assert.assertEquals(1, location.endLineNumber);
    }

    public static void assertLocation(
        int startLineNumber, int startColumn, int endLineNumber, int endColumn, 
        Location location) {
        Assert.assertEquals(startLineNumber, location.startLineNumber);
        Assert.assertEquals(startColumn, location.startColumn);
        Assert.assertEquals(endLineNumber, location.endLineNumber);
        Assert.assertEquals(endColumn, location.endColumn);
    }

    public static void assertLong(int expected, Cell actual) {
        LongCell cell = (LongCell) actual;
        Assert.assertEquals(expected, cell.asLong());
    }

}
