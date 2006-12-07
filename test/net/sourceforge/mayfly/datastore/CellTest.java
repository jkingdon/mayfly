package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.LongLiteral;
import net.sourceforge.mayfly.util.MayflyAssert;

public class CellTest extends TestCase {
    public void testAsInt() throws Exception {
        assertEquals(6, new LongCell(6).asInt());
        assertEquals(6, new IntegerLiteral(6).valueAsCell().asInt());
        assertEquals(6, new LongLiteral(6).valueAsCell().asInt());
    }

    public void testAsLong() throws Exception {
        assertEquals(6L, new LongCell(6).asLong());
    }

    public void testAsString() throws Exception {
        assertEquals("a", new StringCell("a").asString());
    }
    
    public void testCompare() throws Exception {
        assertComparesEqual(new LongCell(6), new LongCell(6));
        MayflyAssert.assertLessThan(new LongCell(6), new LongCell(7));
        
        assertComparesEqual(new StringCell("foo"), new StringCell("foo"));
        MayflyAssert.assertLessThan(new StringCell("11"), new StringCell("5"));

        assertComparesEqual(NullCell.INSTANCE, NullCell.INSTANCE);
        MayflyAssert.assertLessThan(NullCell.INSTANCE, new StringCell(""));
        MayflyAssert.assertLessThan(NullCell.INSTANCE, new LongCell(0));
    }
    
    public void testDateVsString() throws Exception {
        MayflyAssert.assertLessThan(
            new DateCell(2008, 2, 29), new StringCell("2008-03-01"));
        MayflyAssert.assertLessThan(
            new StringCell("1999-12-31"), new DateCell(2000, 01, 01));
        MayflyAssert.assertComparesSqlEqual(
            new DateCell(2008, 11, 23), new StringCell("2008-11-23"));
        
        try {
            MayflyAssert.assertLessThan(new DateCell(2008, 2, 29), new StringCell("someday"));
            fail();
        }
        catch (MayflyException e) {
            assertEquals("'someday' is not in format yyyy-mm-dd",
                e.getMessage());
        }
    }

    private void assertComparesEqual(Cell first, Cell second) {
        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
        
        /**
           The GROUP BY code (in particular, the way that an object of type
           {@link net.sourceforge.mayfly.evaluation.GroupByCells} is the key
           to a map) relies on equals.  So here we test that compareTo
           is consistent with equals().
           
           On the other hand, it doesn't seem
           right for a StringCell to .equals a DateCell, yet
           they might be sqlEquals.  So what is the impact on GROUP BY?
           (perhaps none, if we enforce column types, but this might require
           a bit more looking).
           
           Also consider decimals with the same value but different scales.
         */
        assertEquals(first, second);
        assertEquals(second, first);
    }

    public void testSqlEquals() throws Exception {
        assertSqlEqual(new LongCell(6), new LongCell(6));
        assertNotSqlEqual(new LongCell(6), new LongCell(7));
        
        assertSqlEqual(new StringCell("foo"), new StringCell("foo"));
        assertNotSqlEqual(new StringCell("11"), new StringCell("5"));

        assertNotSqlEqual(NullCell.INSTANCE, NullCell.INSTANCE);
        assertNotSqlEqual(NullCell.INSTANCE, new StringCell(""));
        assertNotSqlEqual(NullCell.INSTANCE, new LongCell(0));
    }

    private void assertSqlEqual(Cell first, Cell second) {
        assertTrue(first.sqlEquals(second));
        assertTrue(second.sqlEquals(first));
    }

    private void assertNotSqlEqual(Cell first, Cell second) {
        assertFalse(first.sqlEquals(second));
        assertFalse(second.sqlEquals(first));
    }

    public void testDisplayName() throws Exception {
        assertEquals("string 'foo'", new StringCell("foo").displayName());
        assertEquals("string 'don''t'", new StringCell("don't").displayName());
        assertEquals("number -5", new LongCell(-5).displayName());
        assertEquals("null", NullCell.INSTANCE.displayName());
    }

}
