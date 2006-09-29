package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.util.ImmutableList;

public class InsertTest extends TestCase {
    
    public void testParse() throws Exception {
        Insert insert = (Insert) Command.fromSql("insert into foo (a, b) values (5, 'Value')");
        assertEquals("some-default", insert.table.schema("some-default"));
        assertEquals("foo", insert.table.tableName());
        assertEquals(ImmutableList.fromArray(new String[] {"a", "b"}), insert.columnNames);

        assertEquals(2, insert.values.size());
        LongCell five = (LongCell) insert.values.cell(0);
        assertEquals(5, five.asLong());
        StringCell string = (StringCell) insert.values.cell(1);
        assertEquals("Value", string.asString());
    }
    
    public void testParseNull() throws Exception {
        Insert insert = (Insert) Command.fromSql("insert into foo (a) values (null)");
        assertEquals("foo", insert.table.tableName());
        assertEquals(ImmutableList.fromArray(new String[] {"a"}), insert.columnNames);

        assertEquals(1, insert.values.size());
        ObjectAssert.assertInstanceOf(NullCell.class, insert.values.cell(0));
    }
    
    public void testParseAll() throws Exception {
        Insert insert = (Insert) Command.fromSql("insert into foo values (5)");
        assertEquals("foo", insert.table.tableName());
        assertNull(insert.columnNames);

        assertEquals(1, insert.values.size());
        LongCell five = (LongCell) insert.values.cell(0);
        assertEquals(5, five.asLong());
    }
    
}
