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

        String sql = "select name from foo where x = (select max(y) from bar)";
        if (dialect.wishThisWereTrue()) {
            assertResultSet(new String[] { " 'five' " },
                query(sql));
        }
        else {
            expectExecuteFailure(sql, "no subselects");
        }
    }
    
    /* Similar case but the subselect has a reference to the foo row.
    */

}
