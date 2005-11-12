package net.sourceforge.mayfly;

import junit.framework.*;

import java.sql.*;
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
        assertEquals(5, database.getInt("foo", "a", 0));

        try {
            database.getInt("foo", "b", 0);
            fail();
        }
        catch (SQLException e) {
            assertEquals("no column b", e.getMessage());
        }

        try {
            database.getInt("bar", "a", 0);
            fail();
        }
        catch (SQLException e) {
            assertEquals("no such table bar", e.getMessage());
        }
    }
    
}
