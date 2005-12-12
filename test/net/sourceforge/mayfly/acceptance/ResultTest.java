package net.sourceforge.mayfly.acceptance;

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

        assertNoColumn(results, 0);
        assertEquals(5, results.getInt(1));
        assertNoColumn(results, 2);

        assertFalse(results.next());
        results.close();
    }

    public void testOrderByDoesNotCountAsWhat() throws Exception {
        execute("create table vehicles (name varchar(255), wheels integer)");
        execute("insert into vehicles (name, wheels) values ('bicycle', 2)");
        ResultSet results = query("select name from vehicles order by wheels");
        assertTrue(results.next());
        assertEquals("bicycle", results.getString(1));
        if (dialect.expectMayflyBehavior()) {
            try {
                results.getInt(2);
                fail();
            } catch (SQLException e) {
                assertMessage("no column 2", e);
            }
        } else {
            // Is this just a hypersonic quirk or do other databases do this?
            assertEquals(2, results.getInt(2));
        }

        results.close();
    }

    public void testTwoMatchingColumns() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("CREATE TABLE BAR (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        execute("INSERT INTO BAR (A) values (7)");
        ResultSet results = query("select foo.a, bar.a from foo inner join bar on bar.a > foo.a");
        assertTrue(results.next());
        if (dialect.expectMayflyBehavior()) {
            try {
                results.getInt("a");
                fail();
            } catch (SQLException e) {
                assertMessage("ambiguous column a", e);
            }
        } else {
            // Seems to be in the confusing "guess what I might mean" category.
            assertEquals(5, results.getInt("a"));
        }
        assertEquals(5, results.getInt(1));
        assertEquals(7, results.getInt(2));
        
        try {
            results.getInt("foo.a");
            fail();
        } catch (SQLException e) {
            assertMessage("column name foo.a should not contain a period", e);
        }
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

    public void testOrderByAmbiguous() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("CREATE TABLE BAR (A INTEGER)");
        String sql = "select foo.a, bar.a from foo, bar order by a";
        if (dialect.expectMayflyBehavior()) {
            expectQueryFailure(sql, "ambiguous column a");
        } else {
            assertResultSet(new String[] { }, query(sql));
        }
    }

    // TODO: order by a   -- where a is in several columns, only one of which survives after the joins
    // TODO: what other cases involving resolving column names?
    
    public void testLimitWithOffset() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (1)");
        execute("insert into foo (a) values (8)");
        execute("insert into foo (a) values (2)");
        execute("insert into foo (a) values (7)");
        execute("insert into foo (a) values (3)");
        execute("insert into foo (a) values (6)");
        execute("insert into foo (a) values (5)");
        execute("insert into foo (a) values (4)");

        assertResultList(new String[] {"4", "5"}, query("select a from foo order by a limit 2 offset 3"));
    
        assertResultList(new String[] {"7", "8"}, query("select a from foo order by a limit 50 offset 6"));

        assertResultList(new String[] { }, query("select a from foo order by a limit 50 offset 8"));
        assertResultList(new String[] { }, query("select a from foo order by a limit 50 offset 9"));

        // Without an ORDER BY, just reject LIMIT (The postgres manual specifically
        // warns against LIMIT without ORDER BY, for example).
        expectQueryFailure("select a from foo limit 2 offset 3", "Must specify ORDER BY with LIMIT");
    }
    
    public void testLimitNoOffset() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (2)");
        execute("insert into foo (a) values (1)");

        assertResultList(new String[] {"1"}, query("select a from foo order by a limit 1"));
        assertResultList(new String[] {"1", "2"}, query("select a from foo order by a limit 2"));
        assertResultList(new String[] {"1", "2"}, query("select a from foo order by a limit 3"));
    }
    

    public void testSelectAll() throws Exception {
        execute("create table foo (x integer, y integer)");
        execute("insert into foo(x, y) values (3, 7)");
        
        ResultSet results = query("select * from foo");
        assertTrue(results.next());

        assertEquals(3, results.getInt(1));
        assertEquals(7, results.getInt(2));
        assertNoColumn(results, 3);

        assertFalse(results.next());
    }

    public void testSelectAllWithJoin() throws Exception {
        execute("create table foo (x integer, y integer)");
        execute("create table bar (x integer, z integer)");
        execute("insert into foo(x, y) values (3, 7)");
        execute("insert into foo(x, y) values (5, 9)");
        execute("insert into bar(x, z) values (3, 80)");
        execute("insert into bar(x, z) values (4, 70)");
        
        ResultSet results = query("select * from foo inner join bar on foo.x = bar.x");
        assertTrue(results.next());

        assertEquals(3, results.getInt(1));
        assertEquals(7, results.getInt(2));
        assertEquals(3, results.getInt(3));
        assertEquals(80, results.getInt(4));
        assertNoColumn(results, 5);

        assertFalse(results.next());
    }

    public void testSelectAllFromTable() throws Exception {
        execute("create table foo (x integer, y integer)");
        execute("create table bar (x integer, z integer)");
        execute("insert into foo(x, y) values (3, 7)");
        execute("insert into foo(x, y) values (5, 9)");
        execute("insert into bar(x, z) values (3, 80)");
        execute("insert into bar(x, z) values (4, 70)");
        
        ResultSet results = query("select bar.* from foo inner join bar on foo.x = bar.x");
        assertTrue(results.next());

        assertEquals(3, results.getInt(1));
        assertEquals(80, results.getInt(2));
        assertNoColumn(results, 3);

        assertFalse(results.next());
    }

    private void assertNoColumn(ResultSet results, int columnIndex) {
        try {
            results.getInt(columnIndex);
            fail();
        } catch (SQLException e) {
            assertMessage("no column " + columnIndex, e);
        }
    }
    
}
