package net.sourceforge.mayfly.acceptance;

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
        // Omitting the FROM is a MySQL extension.  It implicitly implies a single row.
        
        String missingFrom = "select 7";
        if (dialect.fromIsOptional()) {
            assertResultSet(new String[] { " 7 " }, query(missingFrom));
        } else {
            expectQueryFailure(missingFrom, "expected FROM but got end of file");
        }
    }
    
    public void testConcat() throws Exception {
        if (!dialect.verticalBarsMeanConcatenation()) {
            return;
        }

        execute("create table names (first varchar(255), last varchar(255))");
        execute("insert into names(first, last) values ('John', 'Jones')");
        ResultSet results = query("select first || ' ' || last from names");
        assertResultSet(new String[] { "'John Jones'" }, results);
    }

}
