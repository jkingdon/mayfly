package net.sourceforge.mayfly.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
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
import net.sourceforge.mayfly.evaluation.what.AliasedExpression;
import net.sourceforge.mayfly.parser.Location;

public class MayflyAssert {

    public static void assertInteger(int expected, Expression actual) {
        IntegerLiteral ten = (IntegerLiteral) actual;
        assertEquals(expected, ten.value);
    }

    public static void assertString(String expected, Expression actual) {
        QuotedString string = (QuotedString) actual;
        assertEquals(expected, string.stringWithoutQuotes());
    }

    public static void assertColumn(String expectedName, Expression expression) {
        assertColumn(null, expectedName, expression);
    }

    public static void assertColumn(String expectedAlias, String expectedName, 
        Expression expression) {
        SingleColumn y = (SingleColumn) expression;
        assertEquals(expectedAlias, y.tableOrAlias());
        assertEquals(expectedName, y.columnName());
    }

    public static void assertAliasedColumn(String expectedAlias,
        String expectedTable, String expectedName, 
        Expression expression) {
        AliasedExpression aliased = (AliasedExpression) expression;
        assertEquals(expectedAlias, aliased.alias);
        assertColumn(expectedTable, expectedName, aliased.expression);
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
        assertEquals(expectedStartColumn, location.startColumn);
        assertEquals(expectedEndColumn, location.endColumn);
        assertEquals(1, location.startLineNumber);
        assertEquals(1, location.endLineNumber);
    }

    public static void assertLocation(
        int startLineNumber, int startColumn, int endLineNumber, int endColumn, 
        Location location) {
        assertEquals(startLineNumber, location.startLineNumber);
        assertEquals(startColumn, location.startColumn);
        assertEquals(endLineNumber, location.endLineNumber);
        assertEquals(endColumn, location.endColumn);
    }

    public static void assertLong(int expected, Cell actual) {
        LongCell cell = (LongCell) actual;
        assertEquals(expected, cell.asLong());
    }

    public static void assertString(String expected, Cell actual) {
        StringCell cell = (StringCell) actual;
        assertEquals(expected, cell.asString());
    }

    public static void assertLessThan(Cell cell1, Cell cell2) {
        assertFalse(cell1.sqlEquals(cell2));
        assertFalse(cell2.sqlEquals(cell1));
        assertTrue(cell1.compareTo(cell2) < 0);
        assertTrue(cell2.compareTo(cell1) > 0);
    }

    public static void assertComparesSqlEqual(Cell cell1, Cell cell2) {
        assertTrue(cell1.sqlEquals(cell2));
        assertTrue(cell2.sqlEquals(cell1));
        assertEquals(0, cell1.compareTo(cell2));
        assertEquals(0, cell2.compareTo(cell1));
    }

    public static Cell coerce(DataType type, Cell cell) {
        return type.coerce(new Value(cell), "test_column");
    }

}
