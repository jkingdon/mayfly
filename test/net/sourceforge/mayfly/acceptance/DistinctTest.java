package net.sourceforge.mayfly.acceptance;

/**
 * @internal
 * See also {@link net.sourceforge.mayfly.acceptance.GroupByTest#testGroupByActsLikeDistinct()}
 */
public class DistinctTest extends SqlTestCase {

    public void testBasics() throws Exception {
        if (!dialect.wishThisWereTrue()) {
            return;
        }

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
        if (!dialect.wishThisWereTrue()) {
            return;
        }

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

}
