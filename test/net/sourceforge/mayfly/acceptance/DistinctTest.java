package net.sourceforge.mayfly.acceptance;

/**
 * @internal
 * See also {@link net.sourceforge.mayfly.acceptance.GroupByTest#testGroupByActsLikeDistinct()}
 */
public class DistinctTest extends SqlTestCase {

    public void testBasics() throws Exception {
        execute("create table stations(name varchar(255), line varchar(80))");
        execute("insert into stations(name, line) values('Tenleytown', 'Red')");
        execute("insert into stations(name, line) values('Greenbelt', 'Green')");
        execute("insert into stations(name, line) values('Navy Yard', 'Green')");
        
        assertResultList(new String[] { " 'Green' ", " 'Red' " },
            query("select distinct line from stations order by line"));
        assertResultList(new String[] { " 'Green' ", " 'Green' ", " 'Red' " },
            query("select all line from stations order by line"));

        assertResultSet(new String[] { " 'Greenbelt', 'Green' ", 
            " 'Navy Yard', 'Green' ", 
            " 'Tenleytown', 'Red' " },
            query("select distinct name, line from stations"));
    }
    
    public void testWithGroupBy() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(50)");
        execute("insert into foo(a) values(50)");
        execute("insert into foo(a) values(51)");
        execute("insert into foo(a) values(51)");
        
        assertResultList(
            new String[] { " 2 ", " 2 " },
            query("select all count(a) from foo group by a")
        );
        assertResultList(
            new String[] { " 2 " },
            query("select distinct count(a) from foo group by a")
        );
    }

    public void testWithLimit() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(50)");
        execute("insert into foo(a) values(50)");
        execute("insert into foo(a) values(51)");
        execute("insert into foo(a) values(52)");
        execute("insert into foo(a) values(52)");
        execute("insert into foo(a) values(53)");
        
        String withLimit = "select distinct a from foo order by a limit 2";
        if (dialect.haveLimit()) {
            assertResultList(
                new String[] { " 50 ", " 51 " },
                query(withLimit)
                );
            assertResultList(
                new String[] { " 52 ", " 53 " },
                query("select distinct a from foo order by a limit 9999 offset 2")
                );
            assertResultList(
                new String[] { " 51 ", " 52 ", " 52 ", " 53 " },
                query("select all a from foo order by a limit 9999 offset 2")
                );
        }
        else {
            expectQueryFailure(withLimit, "expected end of file but got LIMIT");
        }
    }
    
    public void testWithOrderBy() throws Exception {
        execute("create table foo(a integer, b integer, c integer)");
        
        String orderByNotInSelectList = "select distinct a from foo order by c";
        if (dialect.errorIfOrderByNotInSelectDistinct()) {
            expectQueryFailure(orderByNotInSelectList, 
                "ORDER BY expression c should be in SELECT DISTINCT list");
            expectQueryFailure("select distinct a from foo order by d", 
                "no column d");
        }
        else {
            assertResultList(new String[] { },
                query(orderByNotInSelectList));
        }

        execute("insert into foo(a, b, c) values(5, 10, 300)");
        execute("insert into foo(a, b, c) values(5, 10, 200)");
        execute("insert into foo(a, b, c) values(5, 12, 200)");
        execute("insert into foo(a, b, c) values(6, 10, 100)");

        if (dialect.errorIfOrderByNotInSelectDistinct()) {
            expectQueryFailure(orderByNotInSelectList, 
                "ORDER BY expression c should be in SELECT DISTINCT list");
        }
        else {
            // MySQL (somehow) comes up with 6,5
            // Derby applies the DISTINCT to c (so it seems): 6,5,5
            // Either one seems dubious.
            query(orderByNotInSelectList);
        }

        assertResultList(new String[] { "5, 10", "5, 12", "6, 10" },
            query("select distinct a,b from foo order by a,b")); 
        assertResultList(new String[] { "5, 10", "6, 10", "5, 12" },
            query("select distinct a,b from foo order by b,a")); 
    }

    /**
     * @internal
     * Similar to {@link #testWithOrderBy()} but attempts to show one
     * scenario where this can come up (especially via Hibernate-generated
     * SQL).
     */
    public void testOrderByTwoTableAliases() throws Exception {
        execute("create table country(id integer, name varchar(255))");
        execute("insert into country(id, name) values(5, 'Sri Lanka')");
        execute("insert into country(id, name) values(4, 'India')");
        
        execute("create table cities(name varchar(80), country integer)");
        execute("insert into cities(name, country) values('Colombo', 5)");
        execute("insert into cities(name, country) values('Mumbai', 4)");
        execute("insert into cities(name, country) values('Delhi', 4)");
        
        String sql = "select distinct country1.name " +
            "from cities inner join country country1 " +
            "on cities.country = country1.id " +
            "inner join country country2 " +
            "on cities.country = country2.id " +
            "order by country2.name";
        if (dialect.errorIfOrderByNotInSelectDistinct()) {
            expectQueryFailure(sql, 
                "ORDER BY expression country2.name should be in SELECT DISTINCT list");
        }
        else {
            assertResultList(
                new String[] { " 'India' ", " 'Sri Lanka' " },
                query(sql));
        }
    }

    public void testNull() throws Exception {
        execute("create table foo(a integer, b integer)");
        execute("insert into foo(a, b) values(null, 5)");
        execute("insert into foo(a, b) values(null, 7)");
        
        /* The fact that these are collapsed in DISTINCT kind of makes it look
           like the one null is equal to the other null, so I'm not sure
           how consistent this is with the general semantics of null.
           However, the behavior here seems to be pretty universal among
           SQL implementations.
           The only examples I can think of are pretty contrived, so I'm
           not sure what the real world impact is, one way or the other. */
        assertResultList(new String[] { " null " },
            query("select distinct a from foo"));
        assertResultList(new String[] { " null ", " null " },
            query("select all a from foo"));
    }

}
