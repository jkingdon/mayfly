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
        
        String query = "select distinct line from stations order by line";
        if (!dialect.wishThisWereTrue()) {
            expectQueryFailure(query, "expected expression but got DISTINCT");
        }
        else {
            assertResultList(new String[] { " 'Green' ", " 'Red' " },
                query(query));
            assertResultList(new String[] { " 'Green' ", " 'Green' ", " 'Red' " },
                query("select all line from stations order by line"));
    
            assertResultSet(new String[] { " 'Greenbelt', 'Green' ", 
                " 'Navy Yard', 'Green' ", 
                " 'Tenleytown', 'Red' " },
                query("select distinct name, line from stations"));
        }
    }
    
    public void testWithGroupBy() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(50)");
        execute("insert into foo(a) values(50)");
        execute("insert into foo(a) values(51)");
        execute("insert into foo(a) values(51)");
        
        String query = "select all count(a) from foo group by a";
        
        if (!dialect.wishThisWereTrue()) {
            expectQueryFailure(query, "expected expression but got ALL");
        }
        else {
            assertResultList(
                new String[] { " 2 ", " 2 " },
                query(query)
            );
            assertResultList(
                new String[] { " 2 " },
                query("select distinct count(a) from foo group by a")
            );
        }
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
        if (!dialect.wishThisWereTrue()) {
            expectQueryFailure(withLimit, "expected expression but got DISTINCT");
        }
        else if (dialect.haveLimit()) {
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
    
    // select distinct a from foo order by b -> error?
    // select distinct a,b from foo order by a,b
    // select distinct a,b from foo order by b,a

}
