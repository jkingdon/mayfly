package net.sourceforge.mayfly.acceptance;

import java.sql.SQLException;

public class AggregateTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        execute("insert into foo (x) values (null)");
        execute("insert into foo (x) values (9)");
        
        assertResultSet(new String[] { " 9 " }, query("select max(x) from foo"));
        assertResultSet(new String[] { " 5 " }, query("select min(x) from foo"));
        assertResultSet(new String[] { " 2 " }, query("select count(x) from foo"));
        assertResultSet(new String[] { " 3 " }, query("select count(*) from foo"));
        assertResultSet(new String[] { " 14 " }, query("select sum(x) from foo"));
        assertResultSet(new String[] { " 7 " }, query("select avg(x) from foo"));
    }
    
    public void testSelectingResults() throws Exception {
        execute("create table foo (x integer, y integer)");
        execute("create table bar (x integer, z integer)");
        execute("insert into foo(x, y) values (5, 10)");
        execute("insert into foo(x, y) values (9, 20)");
        execute("insert into bar(x, z) values (9, 100)");
        execute("insert into bar(x, z) values (9, 200)");
        
        // Just to make it clear what this looks like before aggregation:
        assertResultSet(new String[] { "9, 9, 20, 100 ", "9, 9, 20, 200" },
            query("select foo.x, bar.x, y, z from foo inner join bar on foo.x = bar.x")
        );
        // And the real test:
        assertResultSet(new String[] { " 200, 2, 40" }, 
            query("select max(z), count(foo.x), sum(y) from foo inner join bar on foo.x = bar.x")
        );
    }

    public void testColumnAndAggregate() throws Exception {
        execute("create table foo (x integer)");
        
        expectQueryFailure("select x, max(x) from foo", "x is a column but max(x) is an aggregate");
        expectQueryFailure("select X || 'L', Max ( x ) from foo", "X is a column but Max(x) is an aggregate");
        expectQueryFailure("select '#' || x , MAX(X) from foo", "x is a column but MAX(X) is an aggregate");
        expectQueryFailure("select max(x) || 'L', x from foo", "x is a column but max(x) is an aggregate");
        expectQueryFailure("select '#' || max(x) , x from foo", "x is a column but max(x) is an aggregate");
        expectQueryFailure("select foo.*, min(x) from foo", "foo.x is a column but min(x) is an aggregate");
        expectQueryFailure("select x, max(distinct x) from foo", "x is a column but max(distinct x) is an aggregate");
    }
    
    public void testColumnOperatorAggregate() throws Exception {
        execute("create table foo (x integer)");

        String sql = "select x + min(x) from foo";
        if (dialect.disallowColumnAndAggregateInExpression()) {
            expectQueryFailure(sql, "x is a column but min(x) is an aggregate");
        }
        else {
            assertResultSet(new String[] { " null " }, query(sql));
        }
    }
    
    public void testColumnAndCountAll() throws Exception {
        execute("create table foo (x integer)");
        expectQueryFailure("select x, coUNt ( * ) from foo", "x is a column but coUNt(*) is an aggregate");
    }

    public void testLiteralAndAggregate() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        
        assertResultSet(new String[] { " 3, 5 " }, query("select 3, max(x) from foo")); 
    }

    public void testLiteralAndColumn() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        
        assertResultSet(new String[] { " 3, 5 " }, query("select 3, x from foo")); 
    }

    public void testBadColumnName() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        execute("insert into foo (x) values (null)");
        execute("insert into foo (x) values (9)");
        
        expectQueryFailure("select max(y) from foo", "no column y");
    }

    public void testWhere() throws Exception {
        execute("create table foo (x integer, y integer)");
        execute("insert into foo (x, y) values (5, 10)");
        execute("insert into foo (x, y) values (null, 10)");
        execute("insert into foo (x, y) values (9, 9)");
        
        assertResultSet(new String[] { " 5 " }, query("select max(x) from foo where y = 10"));
    }

    public void testAggregateInWhere() throws Exception {
        execute("create table foo (x integer, y integer, z integer)");
        execute("insert into foo (x, y, z) values (5, 10, null)");
        execute("insert into foo (x, y, z) values (null, 10, null)");
        execute("insert into foo (x, y, z) values (9, 9, null)");
        
        expectQueryFailure("select max(x) from foo where count(y) > 0", "aggregate count(y) not valid in WHERE");
        expectQueryFailure("select max(x) from foo where count(z) > 0", "aggregate count(z) not valid in WHERE");
        expectQueryFailure("select x from foo where count(y) > 0", "aggregate count(y) not valid in WHERE");
    }

    public void testNoRows() throws Exception {
        execute("create table foo (x integer)");
        assertResultSet(new String[] { " null " }, query("select max(x) from foo"));
        assertResultSet(new String[] { " null " }, query("select min(x) from foo"));
        assertResultSet(new String[] { " 0 " }, query("select count(x) from foo"));
        assertResultSet(new String[] { " 0 " }, query("select count(*) from foo"));
        assertResultSet(new String[] { " null " }, query("select sum(x) from foo"));
        assertResultSet(new String[] { " null " }, query("select avg(x) from foo"));

        expectQueryFailure("select max(y) from foo", "no column y");
    }

    public void testNullRowsOnly() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (null)");
        assertResultSet(new String[] { " null " }, query("select max(x) from foo"));
        assertResultSet(new String[] { " null " }, query("select min(x) from foo"));
        assertResultSet(new String[] { " 0 " }, query("select count(x) from foo"));
        assertResultSet(new String[] { " 1 " }, query("select count(*) from foo"));
        assertResultSet(new String[] { " null " }, query("select sum(x) from foo"));
        assertResultSet(new String[] { " null " }, query("select avg(x) from foo"));

        expectQueryFailure("select max(y) from foo", "no column y");
    }

    public void testAggregateExpression() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        
        if (dialect.verticalBarsMeanConcatenation()) {
            assertResultSet(new String[] { " 'L5' " }, query("select 'L' || max(x) from foo"));
        } else {
            assertResultSet(new String[] { " 6 " }, query("select 1 + max(x) from foo"));
        }
        expectQueryFailure("select 'L' || max(y) from foo", "no column y");
    }

    public void testCountDistinctAndAll() throws Exception {
        execute("create table foo (x integer, y integer)");
        execute("insert into foo (x, y) values (5, 60)");
        execute("insert into foo (x, y) values (5, 90)");
        execute("insert into foo (x, y) values (7, 90)");
        
        assertResultSet(new String[] { " 3 " }, query("select count(all x) from foo"));
        assertResultSet(new String[] { " 2 " }, query("select count(distinct x) from foo"));

        String distinctStar = "select count(distinct *) from foo";
        if (dialect.allowCountDistinctStar()) {
            assertResultSet(new String[] { " 3 " }, query(distinctStar));
        }
        else {
            expectQueryFailure(distinctStar, "expected primary but got '*'");
        }
    }

    public void testAll() throws Exception {
        execute("create table foo (x integer, y integer)");
        execute("insert into foo (x, y) values (5, 60)");
        execute("insert into foo (x, y) values (5, 90)");
        execute("insert into foo (x, y) values (7, 90)");
        
        assertResultSet(new String[] { " 80 " }, query("select avg(all y) from foo"));
        assertResultSet(new String[] { " 17 " }, query("select sum(all x) from foo"));
        assertResultSet(new String[] { " 5 " }, query("select min(all x) from foo"));
        assertResultSet(new String[] { " 7 " }, query("select max(all x) from foo"));
    }

    public void testDistinct() throws Exception {
        execute("create table foo (x integer, y integer)");
        execute("insert into foo (x, y) values (5, 60)");
        execute("insert into foo (x, y) values (5, 90)");
        execute("insert into foo (x, y) values (7, 90)");
        
        checkDistinct(75, "select avg(distinct y) from foo");

        checkDistinct(12, "select sum(distinct x) from foo");

        // Specifying distinct for Minimum/maximum is kind of pointless,
        // but legal it would seem
        checkDistinct(5, "select min(distinct x) from foo");
        checkDistinct(7, "select max(distinct x) from foo");
    }

    private void checkDistinct(int expected, String sql) throws SQLException {
        if (dialect.aggregateDistinctIsForCountOnly()) {
            expectQueryFailure(sql, null);
        } else {
            assertResultSet(new String[] { "" + expected }, query(sql));
        }
    }
    
    public void testAsteriskOnlyForCount() throws Exception {
        execute("create table foo (x integer, y integer)");
        if (!dialect.aggregateAsteriskIsForCountOnly()) {
            return;
        }
        expectQueryFailure("select avg(*) from foo", "expected primary but got '*'");
        expectQueryFailure("select sum(*) from foo", "expected primary but got '*'");
        expectQueryFailure("select min(*) from foo", "expected primary but got '*'");
        expectQueryFailure("select max(*) from foo", "expected primary but got '*'");
    }

    public void testStrings() throws Exception {
        execute("create table foo (x varchar(255), y varchar(255))");
        execute("insert into foo (x, y) values ('one', 'a')");
        execute("insert into foo (x, y) values ('one', 'b')");
        execute("insert into foo (x, y) values ('two', 'a')");
        
        assertResultSet(new String[] { " 3 " }, query("select count(*) from foo"));
        assertResultSet(new String[] { " 3 " }, query("select count(x) from foo"));
        assertResultSet(new String[] { " 2 " }, query("select count(distinct x) from foo"));

        String sqlForMinimum = "select min(x) from foo";
        if (mayflyMissing()) {
            // is this indeed doing a string sort?
            assertResultSet(new String[] { " 'one' " }, query(sqlForMinimum));
        }
        else {
            expectQueryFailure(sqlForMinimum, "attempt to apply min(x) to string 'one'");
            expectQueryFailure("select max(x) from foo", "attempt to apply max(x) to string 'one'");

            // what about these? Do the databases coerce the string to a number or something?
            expectQueryFailure("select sum(x) from foo", "attempt to apply sum(x) to string 'one'");
            expectQueryFailure("select avg(x) from foo", "attempt to apply avg(x) to string 'one'");
        }
    }
    
    // TODO: String case with no non-null values:
    // Should still get the errors based on the column type....

}
