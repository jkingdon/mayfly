package net.sourceforge.mayfly;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;

import java.util.*;

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
    }

}
