package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;

public class CommandTest extends TestCase {

    public void testParse() throws Exception {
        DropTable command = (DropTable) Command.fromSql("drop table FOO");
        assertEquals("FOO", command.table());
        assertFalse(command.ifExists);
    }
    
    public void testParseSecondExample() throws Exception {
        CreateTable command = (CreateTable) 
            Command.fromSql("Create Table Foo (a integer)");

        assertEquals("Foo", command.table());
        Columns columns = command.columns();
        assertEquals(1, columns.size());
        Column column = (Column) columns.element(0);
        assertEquals("a", column.columnName());
    }
    
}
