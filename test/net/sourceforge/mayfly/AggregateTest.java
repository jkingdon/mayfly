package net.sourceforge.mayfly;


public class AggregateTest extends SqlTestCase {
    
    public void testMax() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        execute("insert into foo (x) values (null)");
        execute("insert into foo (x) values (9)");
        
        assertResultSet(new String[] { " 9 " }, query("select max(x) from foo"));
    }

    public void testColumnAndAggregate() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        
        expectQueryFailure("select x, max(x) from foo", "x is a column but max(x) is an aggregate");
        expectQueryFailure("select X || 'L', Max ( x ) from foo", "X is a column but Max(x) is an aggregate");
        expectQueryFailure("select '#' || x , MAX(X) from foo", "x is a column but MAX(X) is an aggregate");
        expectQueryFailure("select max(x) || 'L', x from foo", "x is a column but max(x) is an aggregate");
        expectQueryFailure("select '#' || max(x) , x from foo", "x is a column but max(x) is an aggregate");
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

    public void testNoRows() throws Exception {
        execute("create table foo (x integer)");
        assertResultSet(new String[] { " null " }, query("select max(x) from foo"));

        if (MAYFLY_MISSING) {
            assertResultSet(new String[] { " null " }, query("select min(x) from foo"));
            assertResultSet(new String[] { " 0 " }, query("select count(x) from foo"));
            assertResultSet(new String[] { " null " }, query("select sum(x) from foo"));
            assertResultSet(new String[] { " null " }, query("select avg(x) from foo"));
        }

        expectQueryFailure("select max(y) from foo", "no column y");
    }

    public void testAggregateExpression() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        
        assertResultSet(new String[] { " 'L5' " }, query("select 'L' || max(x) from foo")); 
        expectQueryFailure("select 'L' || max(y) from foo", "no column y");
    }

    public void testCount() throws Exception {
        if (!MAYFLY_MISSING) {
            return;
        }

        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        execute("insert into foo (x) values (null)");
        execute("insert into foo (x) values (9)");
        
        assertResultSet(new String[] { " 2 " }, query("select count(x) from foo"));
        assertResultSet(new String[] { " 3 " }, query("select count(*) from foo"));
    }

    public void testDistinctAndAll() throws Exception {
        if (!MAYFLY_MISSING) {
            // Needs parser work, first of all
            return;
        }

        execute("create table foo (x integer, y integer)");
        execute("insert into foo (x, y) values (5, 70)");
        execute("insert into foo (x, y) values (5, 90)");
        execute("insert into foo (x, y) values (7, 90)");
        
        assertResultSet(new String[] { " 3 " }, query("select count(all x) from foo"));
        assertResultSet(new String[] { " 2 " }, query("select count(distinct x) from foo"));
    }

}
