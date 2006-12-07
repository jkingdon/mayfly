package net.sourceforge.mayfly.util;

import junit.framework.Assert;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.types.DataType;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.parser.Location;

public class MayflyAssert {

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

    public static void assertColumn(
        String expectedTable, String expectedColumn, String expectedValue, 
        ResultRow row, int index) {
        assertColumn(expectedTable, expectedColumn, row.expression(index));
        assertString(expectedValue, row.cell(index));
    }

    public static void assertColumn(
        String expectedTable, String expectedColumn, int expectedValue, 
        ResultRow row, int index) {
        assertColumn(expectedTable, expectedColumn, row.expression(index));
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

    public static void assertString(String expected, Cell actual) {
        StringCell cell = (StringCell) actual;
        Assert.assertEquals(expected, cell.asString());
    }

    public static void assertLessThan(Cell cell1, Cell cell2) {
        Assert.assertFalse(cell1.sqlEquals(cell2));
        Assert.assertFalse(cell2.sqlEquals(cell1));
        Assert.assertTrue(cell1.compareTo(cell2) < 0);
        Assert.assertTrue(cell2.compareTo(cell1) > 0);
    }

    public static void assertComparesSqlEqual(Cell cell1, Cell cell2) {
        Assert.assertTrue(cell1.sqlEquals(cell2));
        Assert.assertTrue(cell2.sqlEquals(cell1));
        Assert.assertEquals(0, cell1.compareTo(cell2));
        Assert.assertEquals(0, cell2.compareTo(cell1));
    }

    public static Cell coerce(DataType type, Cell cell) {
        return type.coerce(new Value(cell), "test_column");
    }

}
