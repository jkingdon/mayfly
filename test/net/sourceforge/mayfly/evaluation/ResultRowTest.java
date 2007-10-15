package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.Average;
import net.sourceforge.mayfly.evaluation.expression.CountAll;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.util.MayflyAssert;

public class ResultRowTest extends TestCase {
    
    public void testFindColumn() throws Exception {
        ResultRow row = 
            new ResultRow()
                .withColumn("foo", "y", new StringCell("hi"))
                .withColumn("foo", "x", new LongCell(5))
                .withColumn("foo", "z", new LongCell(5))
                .with(new CountAll("count"), new LongCell(15))
                ;
        SingleColumn column = (SingleColumn) row.findColumn("x");
        assertEquals("foo", column.tableOrAlias());
        assertEquals("x", column.columnName());
    }

    public void testFindColumnAmbiguous() throws Exception {
        ResultRow row = 
            new ResultRow()
                .withColumn("foo", "x", new LongCell(5))
                .withColumn("bar", "x", new LongCell(5));
        try {
            row.findColumn("x");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("ambiguous column x", e.getMessage());
        }
    }

    public void testFindColumnWithPeriod() throws Exception {
        ResultRow row = 
            new ResultRow()
                .withColumn("foo", "x", new LongCell(5))
                .withColumn("bar", "x", new LongCell(5));
        try {
            row.findColumn("foo.x");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("column name foo.x should not contain a period", e.getMessage());
        }
    }

    public void testFindColumnNotFound() throws Exception {
        ResultRow row = 
            new ResultRow()
                .withColumn("foo", "x", new LongCell(5))
            ;
        try {
            row.findColumn("y");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("no column y", e.getMessage());
        }
    }
    
    public void testFindColumnSpecifyingTable() throws Exception {
        ResultRow row = 
            new ResultRow()
                .withColumn("foo", "x", new LongCell(5))
                .withColumn("foo", "y", new StringCell("hi"))
                .withColumn("bar", "x", new LongCell(5))
                .with(new CountAll("count"), new LongCell(15))
                ;
        SingleColumn column = row.findColumn("bar", "x");
        assertEquals("bar", column.tableOrAlias());
        assertEquals("x", column.columnName());
        
        try {
            row.findColumn("bar", "y");
            fail();
        }
        catch (NoColumn e) {
            assertEquals("no column bar.y", e.getMessage());
        }
    }

    public void testFindValue() throws Exception {
        ResultRow row = 
            new ResultRow()
                .withColumn("foo", "x", new LongCell(5))
                .withColumn("bar", "x", new LongCell(6))
                .with(new CountAll("count"), new LongCell(2));
        LongCell barX = (LongCell) row.findValue(new SingleColumn("bar", "x"));
        assertEquals(6L, barX.asLong());

        LongCell count = (LongCell) row.findValue(new CountAll("Count"));
        assertEquals(2L, count.asLong());
    }
    
    public void testAdd() throws Exception {
        ResultRow row = new ResultRow();
        assertEquals(0, row.size());
        
        ResultRow biggerRow = row.with(new CountAll("count"), new LongCell(2));
        assertEquals(1, biggerRow.size());
        LongCell value = (LongCell) biggerRow.findValue(new CountAll("count"));
        assertEquals(2L, value.asLong());
    }
    
    public void testLookUpNeedsColumnResolution() throws Exception {
        ResultRow row = new ResultRow()
            .with(new SingleColumn("foo", "x"), new LongCell(7));
        LongCell cell = (LongCell) row.findValueOrNull(new SingleColumn("x"));
        assertEquals(7L, cell.asLong());
    }

    public void testLookUpNeedsDeepColumnResolution() throws Exception {
        ResultRow row = new ResultRow()
            .with(
                new Plus(
                    new IntegerLiteral(6), 
                    new Average(
                        new SingleColumn("foo", "x"),
                        "AVG",
                        false)
                ), 
                new LongCell(7)
            )
            .withColumn("foo", "x", new LongCell(77));
        LongCell cell = (LongCell) row.findValueOrNull(
            new Plus(
                new IntegerLiteral(6),
                new Average(
                    new SingleColumn("x"),
                    "avg",
                    false)
            )
        );
        assertEquals(7L, cell.asLong());
    }

    public void testCombine() throws Exception {
        ResultRow row1 = new ResultRow()
            .withColumn("foo", "colA", "1")
            .withColumn("foo", "colB", "2");

        ResultRow row2 = new ResultRow()
            .withColumn("bar", "colC", "3");

        ResultRow combinedRow = row1.combine(row2);
        assertEquals(3, combinedRow.size());
        MayflyAssert.assertColumn("foo", "colA", "1", combinedRow, 0);
        MayflyAssert.assertColumn("foo", "colB", "2", combinedRow, 1);
        MayflyAssert.assertColumn("bar", "colC", "3", combinedRow, 2);
    }
    
    public void testConflictingAliases() throws Exception {
        ResultRow row1 = new ResultRow()
            .withColumn("table1", "colA", "1")
            .withColumn("table2", "colB", "2");
    
        ResultRow row2 = new ResultRow()
            .withColumn("TABLE1", "colC", "3");

        try {
            row1.combine(row2);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate table name or alias TABLE1", e.getMessage());
        }
    }
    
    public void testOptions() throws Exception {
        Options options = new Options(true);
        Row row = new TupleBuilder()
            .append("x", new LongCell(7))
            .asRow();
        ResultRow resultRow = new ResultRow(row, "foo", options);
        assertEquals(1, resultRow.size());
        SingleColumn column = (SingleColumn) resultRow.expression(0);
        assertTrue(column.options.tableNamesCaseSensitive());
    }
    
}
