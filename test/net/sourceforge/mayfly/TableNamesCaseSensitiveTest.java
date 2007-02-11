package net.sourceforge.mayfly;

import junit.framework.TestCase;

public class TableNamesCaseSensitiveTest extends TestCase {

    private Database database;

    public void setUp() throws Exception {
        database = new Database();
        database.tableNamesCaseSensitive(true);
    }

    public void testOptions() throws Exception {
        Database local = new Database();
        assertFalse(local.options().tableNamesCaseSensitive());
        local.tableNamesCaseSensitive(true);
        assertTrue(local.options().tableNamesCaseSensitive());
    }
    
    public void testSelect() throws Exception {
        database.execute("create table Foo(x integer)");
        expectQueryFailure("select x from FOO", "no table FOO");
        expectQueryFailure("select x from Foo where FOO.x = 5", "no column FOO.x");
        expectQueryFailure("select x from Foo group by FOO.x", "no column FOO.x");
        expectQueryFailure("select x from Foo group by x having FOO.x > 5",
            "no column FOO.x");
        expectQueryFailure("select x from Foo order by FOO.x", "no column FOO.x");
    }
    
    public void testJoins() throws Exception {
        database.execute("create table foo(x integer)");
        database.execute("create table bar(y integer)");
        expectQueryFailure("select * from foo cross join BAR", "no table BAR");
        expectQueryFailure("select * from FOO cross join bar", "no table FOO");
    }

    // Other commands: update, alter table, etc

    public void testDelete() throws Exception {
        database.execute("create table FOO(x integer)");
        expectExecuteFailure("delete from foo", "no table foo");
        expectExecuteFailure("delete from FOO where foo.x = 5", 
            "no column foo.x");
    }
    
    public void testUpdate() throws Exception {
        database.execute("create table foo(x integer)");
        expectExecuteFailure("update Foo set x = 5", "no table Foo");
        expectExecuteFailure("update foo set x = 5 where Foo.x = 7", 
            "no column Foo.x");
    }
    
    // create table - should still reject another table which differs only in case.
    
    private void expectQueryFailure(String sql, String message) {
        try {
            database.query(sql);
            fail();
        }
        catch (MayflyException e) {
            assertEquals(message, e.getMessage());
        }
    }
    
    private void expectExecuteFailure(String sql, String message) {
        try {
            database.execute(sql);
            fail();
        }
        catch (MayflyException e) {
            assertEquals(message, e.getMessage());
        }
    }
    
}
