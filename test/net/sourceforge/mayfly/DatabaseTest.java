package net.sourceforge.mayfly;

import junit.framework.*;

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
    
}
