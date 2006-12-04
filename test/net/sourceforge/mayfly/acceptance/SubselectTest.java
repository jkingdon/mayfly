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
        assertResultSet(
            new String[] { " 'USA' ", " 'India' " },
            query("SELECT name FROM countries candidate" +
            "  WHERE population >= " +
            "    (SELECT max(population) FROM countries other" +
            "        WHERE other.region = candidate.region)"));

        /* "region" in the subselect means "other.region" -
           the innermost possible binding, that is. */
        assertResultSet(
            new String[] { " 'USA' ", " 'India' " },
            query("SELECT name FROM countries candidate" +
            "  WHERE population >= " +
            "    (SELECT max(population) FROM countries other" +
            "        WHERE region = 'Americas')")
        );

        /* Here's what would have happened if region in the previous
           example had been candidate.region.
          
           The subselect sometimes returns null.  */
        assertResultSet(
            new String[] { },
            query("SELECT name FROM countries candidate" +
            "  WHERE population >= " +
            "    (SELECT max(population) FROM countries other" +
            "        WHERE candidate.region = 'Americas')")
        );
    }
    
    public void testReferToEnclosingNoAlias() throws Exception {
        execute("create table foo(x integer, x2 integer, name varchar(10))");
        execute("insert into foo(x, x2, name) values(6, 60, 'six')");
        execute("insert into foo(x, x2, name) values(5, 52, 'five')");
        execute("insert into foo(x, x2, name) values(4, 35, 'four')");

        execute("create table bar(y integer, z integer)");
        execute("insert into bar(y, z) values(60, 6)");
        execute("insert into bar(y, z) values(50, 5)");
        execute("insert into bar(y, z) values(40, 4)");

        /* In this case, x in the subselect refers to the foo row
           without calling it "foo.x" */
        assertResultSet(new String[] { " 'six' ", " 'five' " },
            query("select name from foo where x2 >= " +
                "(select y from bar where z = x)"));
    }
    
    public void testNoRowsMatch() throws Exception {
        execute("create table foo(x integer)");
        execute("insert into foo(x) values(5)");
        
        execute("create table bar(y integer)");
        
        /* Isn't this better written with EXISTS?
           Is it desirable to throw an error except in the EXISTS case?
         */
        assertResultSet(new String[] { " 5 " }, 
            query("select x from foo where " +
                "(select y from bar where y = x) is null"));

        /* Question here is whether we want to always return null for
           the zero row case, or whether there are times we want to
           throw an error or something. */
//        expectQueryFailure(
//            "select x from foo where x = \n" +
//                "(select y from bar where y = 77)", 
//            "subselect expects one row but got 0",
//            2, 2, 2, 
//            
//            /* 34 might make more sense (that is, flag the whole select).
//               But maybe the start of the select in question is OK.  */
//            8);
    }
    
    public void testNestedSubselects() throws Exception {
        execute("create table apples(a integer, ab integer, ac integer)");
        execute("insert into apples(a, ab, ac) values(5, 8, 13)");
        execute("insert into apples(a, ab, ac) values(6, 8, 12)");
        execute("create table bananas(b integer, bc integer)");
        execute("insert into bananas(b, bc) values(8, 13)");
        execute("insert into bananas(b, bc) values(8, 14)");
        execute("create table carrots(c integer)");
        execute("insert into carrots(c) values(13)");
        execute("insert into carrots(c) values(14)");
        
        assertResultSet(new String[] { " 5 " },
            query("select a from apples where ab =" +
                "(select b from bananas where bc =" +
                    "(select c from carrots where c = ac))")
        );
    }
    
    public void testHaving() throws Exception {
        // TODO: subselect in HAVING condition
    }
    
}
