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
    
    public void testInsert() throws Exception {
        database.execute("create table foo(x integer)");
        expectExecuteFailure("insert into Foo(x) values(5)", "no table Foo");
    }
    
    public void testAddColumn() throws Exception {
        database.execute("create table foo(x integer)");
        expectExecuteFailure("alter table Foo add column y integer", 
            "no table Foo");
    }
    
    public void testCreateTable() throws Exception {
        /* It does not seem desirable to allow table names which differ only
           in case, even in the case sensitive case.  I can't think of a 
           use case for wanting table names which differ only in case,
           and generally the goal of all this is so we can check that a usage
           is portable to databases which do and do not have case sensitive
           table names. */
        database.execute("create table foo(x integer)");
        expectExecuteFailure("create table Foo(x integer)", 
            "table foo already exists");
    }
    
    public void testForeignKeys() throws Exception {
        database.execute("create table refd(x integer primary key)");
        expectExecuteFailure(
            "create table refr(x_id integer, " +
                "foreign key(x_id) references REFD(x))", 
            "no table REFD");
    }
    
    public void testForeignKeySelfReference() throws Exception {
        expectExecuteFailure(
            "create table foo(id integer primary key," +
                "parent integer," +
                "foreign key(parent) references FOO(id))", 
            "no table FOO");
    }

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
