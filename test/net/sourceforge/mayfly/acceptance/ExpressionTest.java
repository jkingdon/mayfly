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
        // Omitting the FROM, for those databases which allow it,
        // implicitly implies a single row.
        
        String missingFrom = "select 7";
        if (dialect.fromIsOptional()) {
            assertResultSet(new String[] { " 7 " }, query(missingFrom));
        } else {
            expectQueryFailure(missingFrom, "expected FROM but got end of file");
        }
    }
    
    public void xtestDual() throws Exception {
        // In MySQL, this works from the command line mysql client
        // but here I get "No tables used".  Strangeness in JDBC driver?
        assertResultSet(new String[] { " 7 " }, query("select 7 from dual"));
        // This one gives "No tables used" even from the command line.
        assertResultSet(new String[] { " 'X' " }, query("select * from dual"));
    }
    
    public void testConcat() throws Exception {
        execute("create table names (first varchar(255), last varchar(255))");
        execute("insert into names(first, last) values ('John', 'Jones')");
        ResultSet results;
        if (dialect.verticalBarsMeanConcatenation()) {
            results = query("select first || ' ' || last from names");
        } else {
            results = query("select concat(first, ' ', last) from names");
        }
        assertResultSet(new String[] { "'John Jones'" }, results);
    }

    public void testPlus() throws Exception {
        execute("create table names (birthyear integer, age integer)");
        execute("insert into names(birthyear, age) values (1706, 50)");
        ResultSet results = query("select birthyear + age from names");
        assertResultSet(new String[] { " 1756 " }, results);
    }

    public void testMath() throws Exception {
        execute("create table foo (a integer, b integer, c integer)");
        execute("insert into foo(a, b, c) values (4, 5, 6)");
        execute("insert into foo(a, b, c) values (1, 2, 9)");
        ResultSet results = query("select 2 * a + c / 3 - 1 from foo");
        assertResultSet(new String[] { " 9 ", " 4 " }, results);
    }
    
}
