package net.sourceforge.mayfly;

import java.sql.*;

public class ExpressionTest extends SqlTestCase {

    public void testSelectExpression() throws Exception {
        execute("create table foo (dummy integer)");
        execute("insert into foo(dummy) values(5)");
        assertResultSet(new String[] {"7"}, query("select 7 from foo"));
    }
    
    public void testStringLiteral() throws Exception {
        execute("create table foo (dummy integer)");
        execute("insert into foo(dummy) values(5)");
        assertResultSet(new String[] {" 'val' "}, query("select 'val' from foo"));
    }
    
    public void testMissingFrom() throws Exception {
        // Omitting the FROM is a MySQL extension which we don't provide.
        // TODO: error message should be more like "FROM missing"
        expectQueryFailure("select 7", "unexpected token: 7");
    }
    
    public void testConcat() throws Exception {
        execute("create table names (first varchar(255), last varchar(255))");
        execute("insert into names(first, last) values ('John', 'Jones')");
        ResultSet results = query("select first || ' ' || last from names");
        assertResultSet(new String[] { "'John Jones'" }, results);
    }

}
