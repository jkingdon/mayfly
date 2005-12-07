package net.sourceforge.mayfly;

public class ExpressionTest extends SqlTestCase {

    public void testSelectExpression() throws Exception {
        execute("create table foo (dummy integer)");
        execute("insert into foo(dummy) values(5)");
        assertResultSet(new String[] {"7"}, query("select 7 from foo"));
    }
    
    public void testMissingFrom() throws Exception {
        // Omitting the FROM is a MySQL extension which we don't provide.
        // TODO: error message should be more like "FROM missing"
        expectQueryFailure("select 7", "unexpected token: 7");
    }

}
