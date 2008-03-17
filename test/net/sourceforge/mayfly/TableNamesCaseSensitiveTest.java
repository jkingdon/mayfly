package net.sourceforge.mayfly;

import junit.framework.TestCase;

public class TableNamesCaseSensitiveTest extends TestCase {

    private Database database;

    @Override
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
        expectQueryFailure("select x from FOO", 
            "attempt to refer to table Foo as FOO " +
            "(with case sensitive table names enabled)");
        expectQueryFailure("select x from Foo where FOO.x = 5", "no column FOO.x");
        expectQueryFailure("select x from Foo group by FOO.x", "no column FOO.x");
        expectQueryFailure("select x from Foo group by x having FOO.x > 5",
            "no column FOO.x");
        expectQueryFailure("select x from Foo order by FOO.x", "no column FOO.x");
    }
    
    public void testJoins() throws Exception {
        database.execute("create table foo(x integer)");
        database.execute("create table bar(y integer)");
        expectQueryFailure("select * from foo cross join BAR", 
            "attempt to refer to table bar as BAR " +
            "(with case sensitive table names enabled)");
        expectQueryFailure("select * from FOO cross join bar", 
            "attempt to refer to table foo as FOO " +
            "(with case sensitive table names enabled)");
    }

    public void testSubselect() throws Exception {
        database.execute("create table Foo(x integer)");
        expectQueryFailure("select x from Foo where x = (select x from FOO)", 
            "attempt to refer to table Foo as FOO " +
            "(with case sensitive table names enabled)");
        expectQueryFailure("select x from FOO where x = (select x from Foo)", 
            "attempt to refer to table Foo as FOO " +
            "(with case sensitive table names enabled)");
        
        expectQueryFailure(
            "select x from Foo where Foo.x = (select max(x) from Foo where FOO.x = 5)", 
            "no column FOO.x");
        expectQueryFailure(
            "select x from Foo where FOO.x = (select max(x) from Foo where Foo.x = 5)", 
            "no column FOO.x");
    }

    public void testDelete() throws Exception {
        database.execute("create table FOO(x integer)");
        expectExecuteFailure("delete from foo", 
            "attempt to refer to table FOO as foo " +
            "(with case sensitive table names enabled)");
        expectExecuteFailure("delete from FOO where foo.x = 5", 
            "no column foo.x");
    }
    
    public void testUpdate() throws Exception {
        database.execute("create table foo(x integer)");
        expectExecuteFailure("update Foo set x = 5", 
            "attempt to refer to table foo as Foo " +
            "(with case sensitive table names enabled)");
        expectExecuteFailure("update foo set x = 5 where Foo.x = 7", 
            "no column Foo.x");
    }
    
    public void testInsert() throws Exception {
        database.execute("create table foo(x integer)");
        expectExecuteFailure("insert into Foo(x) values(5)", 
            "attempt to refer to table foo as Foo " +
            "(with case sensitive table names enabled)");
    }
    
    public void testAddColumn() throws Exception {
        database.execute("create table foo(x integer)");
        expectExecuteFailure("alter table Foo add column y integer", 
            "attempt to refer to table foo as Foo " +
            "(with case sensitive table names enabled)");
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
                "attempt to refer to table refd as REFD " +
                "(with case sensitive table names enabled)");
    }
    
    public void testForeignKeySelfReference() throws Exception {
        expectExecuteFailure(
            "create table foo(id integer primary key," +
                "parent integer," +
                "foreign key(parent) references FOO(id))", 
            "no table FOO");
    }
    
    public void testDropTable() throws Exception {
        database.execute("create table foo (x integer)");

        expectExecuteFailure("DROP TABLE FOO IF EXISTS",
            "attempt to refer to table foo as FOO " +
            "(with case sensitive table names enabled)");

        // Now check that it is not gone
        expectExecuteFailure("create table foo (x integer)", 
            "table foo already exists");

        database.execute("DROP TABLE foo IF EXISTS");

        // Now check that it is gone
        database.execute("create table foo (x integer)");
    }
    
    public void testCreateIndex() throws Exception {
        database.execute("create table foo(a integer)");
        expectExecuteFailure("create index an_index on FOO(a)", 
            "attempt to refer to table foo as FOO " +
            "(with case sensitive table names enabled)");
    }
    
    public void testDropIndex() throws Exception {
        database.execute("create table foo(a integer)");
        database.execute("create index an_index on foo(a)");
        expectExecuteFailure("drop index an_index on FOO", 
            "attempt to refer to table foo as FOO " +
            "(with case sensitive table names enabled)");
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
