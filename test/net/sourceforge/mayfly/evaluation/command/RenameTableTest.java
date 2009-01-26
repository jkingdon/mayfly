package net.sourceforge.mayfly.evaluation.command;

import static net.sourceforge.mayfly.acceptance.SqlTestCase.assertResultSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.MayflyException;

import org.junit.Test;

public class RenameTableTest {
    
    @Test
    public void changeCase() throws Exception {
        Database database = new Database();
        database.execute("create table foo(x integer)");
        assertEquals(0, database.execute("alter table foo rename to FOO"));
        assertResultSet(new String[] { }, database.query("select x from foo"));
    }

    @Test
    public void oldCaseNotSpecifiedExactly() throws Exception {
        Database database = new Database();
        database.execute("create table foo(x integer)");
        assertEquals(0, database.execute("alter table FOO rename to bar"));
        assertResultSet(new String[] { }, database.query("select x from bar"));
        assertEquals(1, database.tables().size());
    }
    
    @Test
    public void destinationTableExistsWithDifferentCase() throws Exception {
        Database database = new Database();
        database.execute("create table foo(x integer)");
        database.execute("create table bar(x integer)");
        database.tableNamesCaseSensitive(true);
        try {
            database.execute("alter table foo rename to BAR");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("table BAR already exists; cannot rename foo to BAR", e.getMessage());
        }
    }

}
