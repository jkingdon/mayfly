package net.sourceforge.mayfly;

import java.sql.*;
import java.util.*;

public class ResultTest extends SqlTestCase {
    
    public void testSelectEmpty() throws Exception {
        execute("CREATE TABLE FOO (a INTEGER)");
        ResultSet results = query("select a from foo");
        assertFalse(results.next());
    }

    public void testSelectFromBadTable() throws Exception {
        expectQueryFailure("select a from foo", "no table foo");
    }

    public void testBadColumnName() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        expectQueryFailure("select b from foo", "no column b");
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
    
    public void testOrderBy() throws Exception {
        execute("create table vehicles (name varchar(255), wheels integer, speed integer)");
        execute("insert into vehicles (name, wheels, speed) values ('bicycle', 2, 15)");
        execute("insert into vehicles (name, wheels, speed) values ('car', 4, 100)");
        execute("insert into vehicles (name, wheels, speed) values ('tricycle', 3, 5)");
        assertResultList(new String[] { "'bicycle'", "'tricycle'", "'car'" },
            query("select name from vehicles order by wheels asc")
        );
        assertResultList(new String[] { "'car'", "'tricycle'", "'bicycle'" },
            query("select name from vehicles order by wheels desc")
        );
        assertResultList(new String[] { "'tricycle'", "'bicycle'", "'car'" },
            query("select name from vehicles order by speed")
        );
    }
    
    public void testOrderByWithAlias() throws Exception {
        execute("create table places (id integer, parent integer, name varchar(255))");
        execute("insert into places(id, parent, name) values(10, 1, 'B')");
        execute("insert into places(id, parent, name) values(1, 20, 'A')");
        execute("insert into places(id, parent, name) values(20, 0, 'C')");
        String baseQuery = "select child.name from " +
                "places child LEFT OUTER JOIN places parent " +
                "on child.parent = parent.id";
        assertResultList(new String[] { "'A'", "'B'", "'C'" },
            query(baseQuery + " order by child.id")
        );

        assertResultList(new String[] { "'C'", "'B'", "'A'" },
            query(baseQuery + " order by child.parent")
        );
        
        // This one blows up because NullCell doesn't compare to LongCell.
        // Worry about this later.
//        assertResultList(new String[] { "'C'", "'B'", "'A'" },
//            query(baseQuery + " order by parent.id")
//        );
        
    }
    
    public void testOrderBySeveralColumns() throws Exception {
        execute("create table foo (name varchar(255), major integer, minor integer)");
        execute("insert into foo (name, major, minor) values ('E', 8, 2)");
        execute("insert into foo (name, major, minor) values ('C', 6, 6)");
        execute("insert into foo (name, major, minor) values ('A', 4, 99)");
        execute("insert into foo (name, major, minor) values ('B', 6, 3)");
        execute("insert into foo (name, major, minor) values ('D', 6, 9)");

        assertResultList(new String[] { "'A'", "'B'", "'C'", "'D'", "'E'" },
            query("select name from foo order by major, minor")
        );
    }

    // TODO: order by a, b
    // TODO: order by a   -- where a is in several columns, only one of which survives after the joins
    // TODO: order by a   -- where a is ambiguous
    // TODO: what other cases involving resolving column names?
    
}
