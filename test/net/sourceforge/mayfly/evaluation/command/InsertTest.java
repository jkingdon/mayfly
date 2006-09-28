package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

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
        assertEquals(
            ImmutableList.fromArray(new Object[] {new LongCell(5), new StringCell("Value")}),
            insert.values
        );
    }
    
    public void testParseNull() throws Exception {
        Insert insert = (Insert) Command.fromSql("insert into foo (a) values (null)");
        assertEquals("foo", insert.table.tableName());
        assertEquals(ImmutableList.fromArray(new String[] {"a"}), insert.columnNames);
        assertEquals(
            ImmutableList.fromArray(new Object[] {NullCell.INSTANCE}),
            insert.values
        );
    }
    
    public void testParseAll() throws Exception {
        Insert insert = (Insert) Command.fromSql("insert into foo values (5)");
        assertEquals("foo", insert.table.tableName());
        assertNull(insert.columnNames);
        assertEquals(
            ImmutableList.fromArray(new Object[] {new LongCell(5)}),
            insert.values
        );
    }
    
}
