package net.sourceforge.mayfly;

import junit.framework.*;

import java.sql.*;
import java.util.*;

public class SqlTest extends TestCase {
    
    private Database database;

    public void setUp() {
        database = new Database();
    }

    public void testBadCommand() throws Exception {
        try {
            database.execute("PICK NOSE");
            fail();
        } catch (SQLException expected) {
            assertEquals("cannot parse PICK NOSE", expected.getMessage());
        }
    }
    
    public void testCommandsAreCaseInsensitive() throws Exception {
        try {
            database.execute("DrOp tAbLe FOO");
            fail();
        } catch (SQLException expected) {
            assertEquals("no such table FOO", expected.getMessage());
        }
    }

    public void testDropNonexisting() throws Exception {
        try {
            database.execute("DROP TABLE FOO");
            fail();
        } catch (SQLException expected) {
            assertEquals("no such table FOO", expected.getMessage());
        }
    }

    public void testCreateAndDrop() throws Exception {
        database.execute("CREATE TABLE FOO");
        assertEquals(1, database.tables().size());
        assertEquals("foo", database.tables().iterator().next());

        database.execute("DROP TABLE Foo");
        assertEquals(0, database.tables().size());
    }
    
    public void testCreateWithOneColumn() throws Exception {
        database.execute("CREATE TABLE FOO (X NUMBER)");
        assertEquals(1, database.tables().size());
        assertEquals("foo", database.tables().iterator().next());

        List columns = database.columnNames("Foo");
        assertEquals(1, columns.size());
        assertEquals("X", columns.get(0));
    }
    
    public void testInsert() throws Exception {
        database.execute("CREATE TABLE FOO (X NUMBER)");
        assertEquals(0, database.rowCount("foo"));
        database.execute("INSERT INTO FOO (X) values (5)");
        assertEquals(1, database.rowCount("foo"));
        assertEquals(5, database.getInt("foo", "x", 0));

        try {
            database.getInt("foo", "y", 0);
            fail();
        }
        catch (SQLException e) {
            assertEquals("no column y", e.getMessage());
        }
    }
    
    public void testSelectEmpty() throws Exception {
        database.execute("CREATE TABLE FOO (X NUMBER)");
        ResultSet results = database.query("select x from foo");
        assertFalse(results.next());
    }

    public void testSelectFromBadTable() throws Exception {
        try {
            database.query("select x from foo");
            fail();
        } catch (SQLException e) {
            assertEquals("no such table foo", e.getMessage());
        }
    }

    public void testBadColumnName() throws Exception {
        database.execute("CREATE TABLE FOO (X NUMBER)");
        // TODO: the insert shouldn't be needed
        database.execute("INSERT INTO FOO (X) values (5)");
        try {
            ResultSet results = database.query("select y from foo");
            // TODO: none of this result set stuff should be needed
            results.next();
            results.getInt("y");
            fail();
        } catch (SQLException e) {
            assertEquals("no column y", e.getMessage());
        }
    }

    public void testSelect() throws Exception {
        database.execute("CREATE TABLE FOO (X NUMBER)");
        database.execute("INSERT INTO FOO (X) values (5)");
        ResultSet results = database.query("select x from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("x"));
        assertFalse(results.next());
    }
    
    // Various result set cases:
    // * try to get results before the first next() call
    // * bad name for column
    // * give column number instead of name
    // * more than one column

}
