package net.sourceforge.mayfly.acceptance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    
    public void testExpressionWithDifferentWhitespace() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        ResultSet results = query("select a + 4 from foo");
        assertTrue(results.next());
        if (dialect.canGetValueViaExpression()) {
            assertEquals(9, results.getInt("a+4"));
        }
        else {
            try {
                results.getInt("a+4");
                fail();
            } catch (SQLException e) {
                assertMessage("no column a+4", e);
            }
        }
        results.close();
    }
    
    public void testAs() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        ResultSet results = query("select a + 4 as total from foo");
        assertTrue(results.next());
        assertEquals(9, results.getInt("total"));
        results.close();
    }
    
    public void testAsIsThereButNotReferenced() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        ResultSet results = query("select a as total from foo");
        assertTrue(results.next());
        try {
            results.getInt("a");
            fail();
        }
        catch (SQLException e) {
            assertMessage("no column a", e);
        }
        assertEquals(5, results.getInt(1));
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

    public void testTwoMatchingColumns() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("CREATE TABLE bar (A INTEGER)");
        execute("INSERT INTO foo (A) values (5)");
        execute("INSERT INTO bar (A) values (7)");
        ResultSet results = query(
            "select foo.a, bar.a from foo inner join bar on bar.a > foo.a");
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
            int result = results.getInt("a");
            assertTrue("expected 5 or 7 but was " + result, result == 5 || result == 7);
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
    

    public void testLimitWithOffset() throws Exception {
        if (!dialect.haveLimit()) {
            return;
        }

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
    
    public void testLimitWithInadequateOrderBy() throws Exception {
        if (!dialect.haveLimit()) {
            return;
        }

        execute("create table foo (x integer, y varchar(255))");
        execute("insert into foo (x, y) values (1, 'a')");
        execute("insert into foo (x, y) values (1, 'c')");
        execute("insert into foo (x, y) values (1, 'd')");
        execute("insert into foo (x, y) values (1, 'b')");
        execute("insert into foo (x, y) values (2, 'e')");
        
        // Perhaps we should detect this case and give an error.
        // Under what circumstance?  Making each ORDER BY contain one
        // column which is declared UNIQUE seems like too much(?).
        // Insisting that the actual
        // data returned have an order which is constrained by the ORDER BY
        // might be right, but depends on a test hitting that case.
        // Then again, isn't there a use case where a user interface lets
        // the user ORDER BY, say, last name.  Do we want to insist that
        // the SQL actually say something like "ORDER BY lastname, id"
        // (which probably makes more sense than lettting the database
        // pick a random order, but might be nit-picky to require)?
        
        // Don't know which rows we'll get, but we should get exactly 2 of them.
        ResultSet results = query("select y from foo order by x limit 2");
        assertTrue(results.next());
        assertTrue(results.next());
        assertFalse(results.next());
        results.close();
    }
    
    public void testTopNQuery() throws Exception {
        // Goal here is to get N rows with the lowest values
        // of x, plus all "ties" (rows with the same value of x as
        // the last row).
        
        // In this example, N == 2 so we end up getting 'a'
        // 'b' as part of the "N", and 'c' because it is a tie.
        
        execute("create table foo (x integer, y varchar(255))");
        execute("insert into foo (x, y) values (1, 'a')");
        execute("insert into foo (x, y) values (1, 'c')");
        execute("insert into foo (x, y) values (1, 'b')");
        execute("insert into foo (x, y) values (2, 'e')");

        // There are other ways to write this query (one involves
        // the "RANK() OVER" feature from SQL2003), but this
        // looks like a pretty sane one.
        String topNViaSubselectAndLimit = 
            "SELECT y FROM foo WHERE x <= " +
                "(SELECT x FROM foo ORDER BY x ASC LIMIT 1 OFFSET 1) ";

        if (dialect.haveLimit()) {
            assertResultSet(new String[] { " 'a' ", " 'b' ", " 'c' " },
                query(topNViaSubselectAndLimit)
            );
        }
        else {
            expectQueryFailure(topNViaSubselectAndLimit, "no LIMIT");
        }
    }
    
    public void testLimitNoOffset() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (2)");
        execute("insert into foo (a) values (1)");

        String sql = "select a from foo order by a limit 1";
        if (!dialect.haveLimit()) {
            expectQueryFailure(sql, "expected end of file but got limit");
            return;
        }
        assertResultList(new String[] {"1"}, query(sql));
        assertResultList(new String[] {"1", "2"}, query("select a from foo order by a limit 2"));
        assertResultList(new String[] {"1", "2"}, query("select a from foo order by a limit 3"));
    }
    
    public void testLimitOffsetAndParameters() throws Exception {
        if (!dialect.haveLimit()) {
            return;
        }
        
        execute("create table foo(x integer)");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        execute("insert into foo(x) values(9)");
        execute("insert into foo(x) values(4)");
        
        PreparedStatement query =
            connection.prepareStatement(
                "select x from foo order by x limit ? offset ?");
        query.setInt(1, 2);
        query.setInt(2, 1);
        assertResultSet(
            new String[] { " 5 ", " 7 " },
            query.executeQuery());
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
