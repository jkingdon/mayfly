package net.sourceforge.mayfly;

import net.sourceforge.mayfly.util.*;

import java.sql.*;
import java.util.*;

public class SqlTest extends SqlTestCase {
    
    public void testBadCommand() throws Exception {
        try {
            execute("PICK NOSE");
            fail();
        } catch (SQLException expected) {
            assertMessage("cannot parse PICK NOSE", expected);
        }
    }

    public void testCommandsAreCaseInsensitive() throws Exception {
        try {
            execute("DrOp tAbLe FOO");
            fail();
        } catch (SQLException expected) {
            assertMessage("no such table FOO", expected);
        }
    }

    public void testDropNonexisting() throws Exception {
        try {
            execute("DROP TABLE FOO");
            fail();
        } catch (SQLException expected) {
            assertMessage("no such table FOO", expected);
        }
    }
    
    public void testTablesMissingOnCreate() throws Exception {
        // CREATE TABLE FOO => should be SQLException
    }

    public void testInsertWithBadColumnName() throws Exception {
        execute("CREATE TABLE FOO (A integer)");
        try {
            execute("INSERT INTO FOO (b) values (5)");
            fail();
        }
        catch (SQLException e) {
            assertMessage("no column b", e);
        }
    }
    
    public void testSelectEmpty() throws Exception {
        execute("CREATE TABLE FOO (a INTEGER)");
        ResultSet results = query("select a from foo");
        assertFalse(results.next());
    }

    public void testSelectFromBadTable() throws Exception {
        try {
            query("select a from foo");
            fail();
        } catch (SQLException e) {
            assertMessage("no such table foo", e);
        }
    }

    public void testBadColumnName() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        try {
            query("select b from foo");
            fail();
        } catch (SQLException e) {
            assertMessage("no column b", e);
        }
    }

    public void testSelect() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("a"));
        assertFalse(results.next());
    }
    
    public void testAskResultSetForUnqueriedColumn() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        try {
            results.getInt("b");
            fail();
        } catch (SQLException e) {
            assertMessage("no column b", e);
        }
    }
    
    public void testTryToGetResultsBeforeCallingNext() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        try {
            results.getInt("a");
            fail();
        } catch (SQLException e) {
            assertMessage("no current result row", e);
        }
    }
    
    public void testTryToGetResultsAfterNextReturnsFalse() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        assertFalse(results.next());
        try {
            results.getInt("a");
            fail();
        } catch (SQLException e) {
            assertMessage("already read last result row", e);
        }
    }
    
    public void testTwoRows() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        execute("INSERT INTO FOO (a) values (7)");
        ResultSet results = query("select a from foo");
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
        execute("CREATE TABLE FOO (A INTEGER, b INTEGER)");
        execute("INSERT INTO FOO (a, B) values (5, 25)");
        ResultSet results = query("select A, b from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("a"));
        assertEquals(25, results.getInt("B"));
        assertFalse(results.next());
    }
    
    public void testColumnNumbers() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());

        try {
            results.getInt(0);
            fail();
        } catch (SQLException e) {
            assertMessage("no column 0", e);
        }

        assertEquals(5, results.getInt(1));

        try {
            results.getInt(2);
            fail();
        } catch (SQLException e) {
            assertMessage("no column 2", e);
        }

        assertFalse(results.next());
    }
    
    public void testWhere() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (4, 16)");
        execute("insert into foo (a, b) values (5, 25)");
        ResultSet results = query("select a, b from foo where b = 25");
        assertTrue(results.next());
        
        assertEquals(5, results.getInt("a"));
        assertEquals(25, results.getInt("b"));
        
        assertFalse(results.next());
    }
    
    public void testSimpleJoin() throws Exception {
        execute("create table foo (a integer)");
        execute("create table bar (b integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (b) values (100)");
        execute("insert into bar (b) values (101)");
        ResultSet results = query("select foo.a, bar.b from foo, bar");
        
        Set expected = new HashSet();
        expected.add(L.fromArray(new int[] {4, 100}));
        expected.add(L.fromArray(new int[] {4, 101}));
        expected.add(L.fromArray(new int[] {5, 100}));
        expected.add(L.fromArray(new int[] {5, 101}));
        
        assertEquals(expected, intResultsAsSet(results, Arrays.asList(new String[] {"a", "b"})));
    }

    public void xtestJoinSameNameTwice() throws Exception {
        execute("create table foo (a integer)");
        execute("create table bar (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (a) values (100)");
        execute("insert into bar (a) values (101)");
        ResultSet results = query("select foo.a, bar.a from foo, bar");
        
        Set expected = new HashSet();
        expected.add(L.fromArray(new int[] {4, 100}));
        expected.add(L.fromArray(new int[] {4, 101}));
        expected.add(L.fromArray(new int[] {5, 100}));
        expected.add(L.fromArray(new int[] {5, 101}));
        
        assertEquals(expected, intResultsAsSet(results, L.fromArray(new int[] {1, 2})));
    }

    private Set intResultsAsSet(ResultSet results, List columns) throws SQLException {
        Set actual = new HashSet();
        while (results.next()) {
            L row = new L();
            for (int i = 0; i < columns.size(); i++) {
                Object column = columns.get(i);
                if (column instanceof String) {
                    row.add(results.getInt((String) column));
                } else {
                    row.add(results.getInt(((Integer) column).intValue()));
                }
            }
            actual.add(row);
        }
        results.close();
        return actual;
    }

    public void testAlias() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (10)");
        ResultSet results = query("select f.a from foo f where f.a = 4");
        assertTrue(results.next());
        
        assertEquals(4, results.getInt("a"));
        
        assertFalse(results.next());
    }
    
}
