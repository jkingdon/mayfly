package net.sourceforge.mayfly;

import junit.framework.TestCase;

import net.sourceforge.mayfly.acceptance.SqlTestCase;
import net.sourceforge.mayfly.datastore.DataStore;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class DatabaseTest extends TestCase {

    private Database database;

    public void setUp() throws Exception {
        database = new Database();
    }

    public void testCreateAndDrop() throws Exception {
        database.execute("CREATE TABLE FOO (A integer)");
        assertEquals(Collections.singleton("FOO"), database.tables());

        database.execute("DROP TABLE Foo");
        assertEquals(Collections.EMPTY_SET, database.tables());
    }
    
    public void testCreateWithOneColumn() throws Exception {
        database.execute("CREATE TABLE Foo (A integer)");
        assertEquals(Collections.singleton("Foo"), database.tables());
        assertEquals(Collections.singletonList("A"), database.columnNames("fOo"));
    }
    
    public void testInsert() throws Exception {
        database.execute("CREATE TABLE FOO (A integer)");
        assertEquals(0, database.rowCount("foo"));
        database.execute("INSERT INTO FOO (A) values (5)");
        assertEquals(1, database.rowCount("foo"));
    }
    
    public void testSnapshot() throws Exception {
        Database original = new Database();
        original.execute("create table foo (a integer)");
        original.execute("insert into foo(a) values(6)");
        DataStore dataStore = original.dataStore();

        Database snapshot = new Database(dataStore);
        snapshot.execute("insert into foo (a) values (70)");
        snapshot.execute("create table bar (b integer)");
        
        original.execute("create table foo2 (c integer)");

        assertEquals(new TreeSet(Arrays.asList(new String[] {"foo", "bar"})), snapshot.tables());
        assertEquals(2, snapshot.rowCount("Foo"));

        assertEquals(new TreeSet(Arrays.asList(new String[] {"foo", "foo2"})), original.tables());
        assertEquals(1, original.rowCount("Foo"));
    }
    
    public void testTables() throws Exception {
        database.execute("create table inAnonymousSchema (x integer)");
        database.execute("create schema mars authorization dba create table foo (x integer)");
        assertEquals(Collections.singleton("inAnonymousSchema"), database.tables());
        database.execute("set schema mars");
        assertEquals(Collections.singleton("foo"), database.tables());
    }
    
    public void testColumnNames() throws Exception {
        database.execute("create table inAnonymousSchema (x integer)");
        database.execute("create schema mars authorization dba create table foo (y integer)");
        assertEquals(Collections.singletonList("x"), database.columnNames("inAnonymousSchema"));
        database.execute("set schema mars");
        assertEquals(Collections.singletonList("y"), database.columnNames("foo"));
        
        try {
            database.columnNames("nosuch");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("no table nosuch", e.getMessage());
        }
    }
    
    public void testQueryAndSchema() throws Exception {
        database.execute("create table inAnonymousSchema (x integer)");
        database.execute("create schema mars authorization dba create table foo (y integer)");
        String fooQuery = "select * from foo";
        try {
            database.query(fooQuery);
            fail();
        } catch (MayflyException e) {
            assertEquals("no table foo", e.getMessage());
        }
        database.execute("set schema mars");
        SqlTestCase.assertResultSet(new String[] { }, database.query(fooQuery));
    }
    
    public void testSchemas() throws Exception {
        database.execute("create schema MARS authorization dba create table foo (x integer)");
        database.execute("create schema Venus authorization dba create table foo (x integer)");
        Set expected = new TreeSet();
        expected.add("MARS");
        expected.add("Venus");
        assertEquals(expected, database.schemas());
    }
    
    public void testScript() throws Exception {
        Reader script = new StringReader(
            "create table foo (x integer); insert into foo(x) values(5)" +
            ";insert into foo(x)values(7)"
        );
        database.executeScript(script);
        SqlTestCase.assertResultSet(
            new String[] { "5", "7" },
            database.query("select x from foo")
        );
    }
    
    public void testScriptError() throws Exception {
        String command = "create table foo\n" +
            "   (x integer,\n" +
            "    y not)";
        Reader script = new StringReader(
            command + ";" +
            "create table otherTable(a integer);");
        try {
            database.executeScript(script);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("expected data type but got NOT", e.getMessage());
            assertEquals(3, e.startLineNumber());
            assertEquals(7, e.startColumn());
            assertEquals(3, e.endLineNumber());
            assertEquals(10, e.endColumn());
            assertEquals(command, e.failingCommand());
        }
    }
    
    public void testOptions() throws Exception {
        assertFalse(database.options().tableNamesCaseSensitive());
        database.tableNamesCaseSensitive(true);
        assertTrue(database.options().tableNamesCaseSensitive());
    }
    
    public void testTableNamesCaseSensitive() throws Exception {
        database.tableNamesCaseSensitive(true);
        database.execute("create table Foo(x integer)");
        expectQueryFailure("select x from FOO", "no table FOO");
        expectQueryFailure("select x from Foo where FOO.x = 5", "no column FOO.x");
        expectQueryFailure("select x from Foo group by FOO.x", "no column FOO.x");
        expectQueryFailure("select x from Foo group by x having FOO.x > 5",
            "no column FOO.x");
        expectQueryFailure("select x from Foo order by FOO.x", "no column FOO.x");
    }
    
    public void testTableNamesCaseSensitiveInJoins() throws Exception {
        database.tableNamesCaseSensitive(true);
        database.execute("create table foo(x integer)");
        database.execute("create table bar(y integer)");
        expectQueryFailure("select * from foo cross join BAR", "no table BAR");
        expectQueryFailure("select * from FOO cross join bar", "no table FOO");
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
    
    // Other commands: update, alter table, etc

    public void testTableNamesCaseSensitiveInDelete() throws Exception {
        database.tableNamesCaseSensitive(true);
        database.execute("create table FOO(x integer)");
        expectExecuteFailure("delete from foo", "no table foo");
//        expectExecuteFailure("delete from FOO where foo.x = 5", "no table foo");
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
