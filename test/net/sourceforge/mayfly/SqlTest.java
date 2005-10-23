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
        database.execute("CREATE TABLE FOO (A NUMBER)");
        assertEquals(1, database.tables().size());
        assertEquals("foo", database.tables().iterator().next());

        List columns = database.columnNames("Foo");
        assertEquals(1, columns.size());
        assertEquals("A", columns.get(0));
    }
    
    public void testInsertWithBadColumnName() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
        try {
            database.execute("INSERT INTO FOO (b) values (5)");
            fail();
        }
        catch (SQLException e) {
            assertEquals("no column b", e.getMessage());
        }
    }
    
    public void testInsert() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
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
    }
    
    public void testSelectEmpty() throws Exception {
        database.execute("CREATE TABLE FOO (a NUMBER)");
        ResultSet results = database.query("select a from foo");
        assertFalse(results.next());
    }

    public void testSelectFromBadTable() throws Exception {
        try {
            database.query("select a from foo");
            fail();
        } catch (SQLException e) {
            assertEquals("no such table foo", e.getMessage());
        }
    }

    public void testBadColumnName() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
        try {
            database.query("select b from foo");
            fail();
        } catch (SQLException e) {
            assertEquals("no column b", e.getMessage());
        }
    }

    public void testSelect() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
        database.execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = database.query("select a from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("a"));
        assertFalse(results.next());
    }
    
    public void testAskResultSetForUnqueriedColumn() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
        database.execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = database.query("select a from foo");
        assertTrue(results.next());
        try {
            results.getInt("b");
            fail();
        } catch (SQLException e) {
            assertEquals("no column b", e.getMessage());
        }
    }
    
    public void testTryToGetResultsBeforeCallingNext() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
        database.execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = database.query("select a from foo");
        try {
            results.getInt("a");
            fail();
        } catch (SQLException e) {
            assertEquals("no current result row", e.getMessage());
        }
    }
    
    public void testTryToGetResultsAfterNextReturnsFalse() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
        database.execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = database.query("select a from foo");
        assertTrue(results.next());
        assertFalse(results.next());
        try {
            results.getInt("a");
            fail();
        } catch (SQLException e) {
            assertEquals("already read last result row", e.getMessage());
        }
    }
    
    public void testTwoRows() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
        database.execute("INSERT INTO FOO (A) values (5)");
        database.execute("INSERT INTO FOO (a) values (7)");
        ResultSet results = database.query("select a from foo");
        assertTrue(results.next());
        int firstResult = results.getInt("a");
        assertTrue(results.next());
        int secondResult = results.getInt("a");
        assertFalse(results.next());
        
        Set expected = new HashSet(Arrays.asList(new Integer[] {new Integer(5), new Integer(7)}));
        Set actual = new HashSet();
        actual.add(new Integer(firstResult));
        actual.add(new Integer(secondResult));
        assertEquals(expected, actual);
    }
    
    public void testMultipleColumns() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER, b NUMBER)");
        database.execute("INSERT INTO FOO (a, B) values (5, 25)");
        ResultSet results = database.query("select A, b from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("a"));
        assertEquals(25, results.getInt("B"));
        assertFalse(results.next());
    }
    
    public void testColumnNumbers() throws Exception {
        database.execute("CREATE TABLE FOO (A NUMBER)");
        database.execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = database.query("select a from foo");
        assertTrue(results.next());

        try {
            results.getInt(0);
            fail();
        } catch (SQLException e) {
            assertEquals("no column 0", e.getMessage());
        }

        assertEquals(5, results.getInt(1));

        try {
            results.getInt(2);
            fail();
        } catch (SQLException e) {
            assertEquals("no column 2", e.getMessage());
        }

        assertFalse(results.next());
    }
    
    public void testWhere() throws Exception {
        database.execute("create table foo (a integer, b integer)");
        database.execute("insert into foo (a, b) values (4, 16)");
        database.execute("insert into foo (a, b) values (5, 25)");
        ResultSet results = database.query("select a, b from foo where b = 25");
        assertTrue(results.next());
        
        assertEquals(5, results.getInt("a"));
        assertEquals(25, results.getInt("b"));
        
        assertFalse(results.next());
    }

}
