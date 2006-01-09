package net.sourceforge.mayfly.acceptance;

import java.sql.*;
import java.util.*;

public class ResultTest extends SqlTestCase {
    
    public void testSelectEmpty() throws Exception {
        execute("CREATE TABLE foo (a INTEGER)");
        ResultSet results = query("select a from foo");
        assertFalse(results.next());
        results.close();
    }

    public void testSelectFromBadTable() throws Exception {
        expectQueryFailure("select a from foo", "no table foo");
    }

    public void testBadColumnName() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        expectQueryFailure("select b from foo", "no column b");
    }

    public void testSelect() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("a"));
        assertFalse(results.next());
        results.close();
    }
    
    public void testAskResultSetForUnqueriedColumn() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        try {
            results.getInt("b");
            fail();
        } catch (SQLException e) {
            assertMessage("no column b", e);
        }
        results.close();
    }
    
    public void testExpression() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        ResultSet results = query("select a + 4 from foo");
        assertTrue(results.next());
        if (dialect.canGetValueViaExpressionName()) {
            assertEquals(9, results.getInt("a + 4"));
            try {
                results.getInt("a+4");
                fail();
            } catch (SQLException e) {
                assertMessage("no column a+4", e);
            }
        }
        else {
            try {
                results.getInt("a + 4");
                fail();
            } catch (SQLException e) {
                assertMessage("no column a + 4", e);
            }
        }
        results.close();
    }
    
    public void testAs() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        ResultSet results = query("select a + 4 as total from foo");
        assertTrue(results.next());
        assertEquals(9, results.getInt("total"));
        results.close();
    }
    
    public void testTryToGetResultsBeforeCallingNext() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        ResultSet results = query("select a from foo");
        try {
            results.getInt("a");
            fail();
        } catch (SQLException e) {
            assertMessage("no current result row", e);
        }
    }
    
    public void testTryToGetResultsAfterNextReturnsFalse() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
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
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        execute("INSERT INTO foo (a) values (7)");
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
        execute("CREATE TABLE foo (A INTEGER, b INTEGER)");
        execute("INSERT INTO foo (a, B) values (5, 25)");
        ResultSet results = query("select A, b from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("a"));
        assertEquals(25, results.getInt("B"));
        assertFalse(results.next());
    }
    
    public void testColumnNumbers() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
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
        if (!dialect.orderByCountsAsWhat()) {
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
        execute("CREATE TABLE foo (A INTEGER)");
        execute("CREATE TABLE bar (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        execute("INSERT INTO bar (A) values (7)");
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
        
        if (dialect.maySpecifyTableDotColumnToJdbc()) {
            // I guess this would make sense.  Maybe we'll get around to implementing it in Mayfly.
            assertEquals(5, results.getInt("foo.a"));
        } else {
            try {
                results.getInt("foo.a");
                fail();
            } catch (SQLException e) {
                assertMessage("column name foo.a should not contain a period", e);
            }
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
    
    public void testOrderByExpression() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo(a, b) values (5, 30)");
        execute("insert into foo(a, b) values (8, 40)");
        execute("insert into foo(a, b) values (3, 50)");
        execute("insert into foo(a, b) values (4, 60)");
        execute("insert into foo(a, b) values (2, 70)");

        String expression = "select a from foo order by a + b";
        // So here's the evil part: an integer is not an expression, it is a special case
        String reference = "select a from foo order by 1, b";
        String referenceDescending = "select a from foo order by 1 desc, b";
        // But this one is an expression
        String constantExpression = "select a from foo order by 1 + 0, b";

        // This one isn't quite so strange; maybe this is worth supporting
        String referenceToExpression = "select a + b from foo order by 1, b";

        assertResultList(new String[] { "2", "3", "4", "5", "8" }, query(reference));
        assertResultList(new String[] { "8", "5", "4", "3", "2" }, query(referenceDescending));
        if (dialect.canOrderByExpression()) {
            assertResultList(new String[] { "5", "8", "3", "4", "2" }, query(expression));
            // Evil!  We can at the very least give an error on a constant expression, I hope
            assertResultList(new String[] { "5", "8", "3", "4", "2" }, query(constantExpression));

            assertResultList(new String[] { "35", "48", "53", "64", "72" }, query(referenceToExpression));
        }
        else {
            expectQueryFailure(expression, "expected end of file but got PLUS");
            expectQueryFailure(constantExpression, "expected end of file but got PLUS");
            
            expectQueryFailure(referenceToExpression, "ORDER BY 1 refers to an expression not a column");
        }
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
        execute("CREATE TABLE foo (A INTEGER)");
        execute("CREATE TABLE bar (A INTEGER)");
        String sql = "select foo.a, bar.a from foo, bar order by a";
        if (dialect.detectsAmbiguousColumns()) {
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
        String limitWithoutOrderBy = "select a from foo limit 2 offset 3";
        if (!dialect.canHaveLimitWithoutOrderBy()) {
            expectQueryFailure(limitWithoutOrderBy, "Must specify ORDER BY with LIMIT");
        } else {
            // Don't know which rows we'll get, but we should get exactly 2 of them.
            ResultSet results = query(limitWithoutOrderBy);
            assertTrue(results.next());
            assertTrue(results.next());
            assertFalse(results.next());
            results.close();
        }
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
