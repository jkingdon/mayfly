package net.sourceforge.mayfly.acceptance;

public class SubselectTest extends SqlTestCase {
    
    public void testAggregate() throws Exception {
        execute("create table foo(x integer, name varchar(10))");
        execute("insert into foo(x, name) values(6, 'six')");
        execute("insert into foo(x, name) values(5, 'five')");
        execute("insert into foo(x, name) values(4, 'four')");

        execute("create table bar(y integer)");
        execute("insert into bar(y) values(5)");
        execute("insert into bar(y) values(2)");
        execute("insert into bar(y) values(-7)");

        assertResultSet(new String[] { " 'five' " },
            query("select name from foo where x = (select max(y) from bar)"));
    }
    
    /**
     * @internal
     * The subselect doesn't need to be an aggregate; anything which
     * returns a single row will do.
     * Similar to the technique in {@link ResultTest#testTopNQuery()}
     */
    public void testOneRow() throws Exception {
        execute("create table countries(id integer, name varchar(255))");
        execute("insert into countries values(1, 'Australia')");
        execute("insert into countries values(2, 'Sri Lanka')");
        execute("insert into countries values(3, 'India')");
        
        execute("create table cities(country integer, name varchar(80))");
        execute("insert into cities(country, name) values (1, 'Perth')");
        execute("insert into cities(country, name) values (3, 'Mumbai')");
        
        assertResultSet(new String[] { " 'Australia' " },
            query("select name from countries where id = " +
                "(select country from cities where name = 'Perth')"));
    }
    
    public void testDelete() throws Exception {
        execute("create table foo(x integer, name varchar(10))");
        execute("insert into foo(x, name) values(6, 'six')");
        execute("insert into foo(x, name) values(5, 'five')");
        execute("insert into foo(x, name) values(4, 'four')");

        execute("create table bar(y integer)");
        execute("insert into bar(y) values(5)");
        execute("insert into bar(y) values(2)");
        
        String sql = "delete from foo where x = (select max(y) from bar)";
        if (dialect.wishThisWereTrue()) {
            assertEquals(1,
                execute(sql));
            assertResultSet(new String[] { "4", "6" }, query("select x from foo"));
        }
        else {
            expectExecuteFailure(sql, 
                "subselects are not yet implemented in this context");
        }
    }
    
    public void testReferToRowInEnclosingQuery() throws Exception {
        execute("create table countries(" +
            "region varchar(255), name varchar(255), population integer)");
        execute("insert into countries values('Americas', 'USA', 300)");
        execute("insert into countries values('Americas', 'Canada', 32)");
        execute("insert into countries values('Asia', 'India', 1000)");

        // Now select the largest country in each region:
        String subselectRefersOutside = 
            "SELECT name FROM countries candidate" +
            "  WHERE population >= " +
            "    (SELECT max(population) FROM countries other" +
            "        WHERE other.region = candidate.region)";
        if (dialect.wishThisWereTrue()) {
            assertResultSet(
                new String[] { " 'USA' ", " 'India' " },
                query(subselectRefersOutside));
        }
        else {
            expectQueryFailure(subselectRefersOutside, 
                "no column candidate.region");
        }

        String shadowedNamesBindToInnermost = 
            "SELECT name FROM countries candidate" +
            "  WHERE population >= " +
            "    (SELECT max(population) FROM countries other" +
            "        WHERE region = 'Americas')";
        /* "region" in the subselect means "other.region" */
        assertResultSet(
            new String[] { " 'USA' ", " 'India' " },
            query(shadowedNamesBindToInnermost)
        );

        String forComparison = 
            "SELECT name FROM countries candidate" +
            "  WHERE population >= " +
            "    (SELECT max(population) FROM countries other" +
            "        WHERE candidate.region = 'Americas')";
        if (dialect.wishThisWereTrue()) {
            /* The subselect sometimes returns null.  */
            assertResultSet(
                new String[] { },
                query(forComparison)
            );
        }
        else {
            expectQueryFailure(subselectRefersOutside, 
                "no column candidate.region");
        }
    }
    
    // TODO: case where the subselect refers to the enclosing row without
    // qualifying it with an alias.
    
}
