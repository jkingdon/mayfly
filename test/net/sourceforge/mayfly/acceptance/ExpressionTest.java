package net.sourceforge.mayfly.acceptance;

import java.sql.ResultSet;

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
    
    public void testConcatenateVerticalBars() throws Exception {
        execute("create table names (first_name varchar(255), last_name varchar(255))");
        execute("insert into names(first_name, last_name) values ('John', 'Jones')");
        ResultSet results;
        if (dialect.verticalBarsMeanConcatenation()) {
            results = query("select first_name || ' ' || last_name from names");
        } else {
            results = query("select concat(first_name, ' ', last_name) from names");
        }
        assertResultSet(new String[] { "'John Jones'" }, results);
    }

    public void testConcatenateStringAndInteger() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        
        String sql;
        if (dialect.verticalBarsMeanConcatenation()) {
            sql = "select 'L' || x from foo";
        } else {
            sql = "select concat('L', x) from foo";
        }

        if (dialect.canConcatenateStringAndInteger()) {
            assertResultSet(new String[] { " 'L5' " }, query(sql));
        } else {
            expectQueryFailure(sql, "cannot convert integer to string");
        }
    }
    
    public void testConcatBuiltIn() throws Exception {
        execute("create table names (first_name varchar(255), last_name varchar(255))");
        execute("insert into names(first_name, last_name) values ('John', 'Jones')");
        String query = "select concat(first_name, ' ', last_name) from names";
        if (dialect.haveConcatBuiltIn()) {
            assertResultSet(new String[] { "'John Jones'" }, query(query));
        } else {
            expectQueryFailure(query, "no function concat");
        }
    }
    
    public void testNoSuchBuiltIn() throws Exception {
        execute("create table foo(x integer)");
        expectExecuteFailure("insert into foo(x) values(ZETA_FUNCTION(5))",
            "no function ZETA_FUNCTION");
    }

    public void testPlus() throws Exception {
        execute("create table names (birthyear integer, age integer)");
        execute("insert into names(birthyear, age) values (1706, 50)");
        assertResultSet(new String[] { " 1756 " }, 
            query("select birthyear + age from names"));
    }

    public void testNullAndMath() throws Exception {
        execute("create table names (" +
            "birthyear integer, age integer, name varchar(255))");
        execute("insert into names(birthyear, age, name) " +
            "values (1706, null, 'no-age')");
        execute("insert into names(birthyear, age, name) " +
            "values (null, 13, 'no-year')");
        execute("insert into names(birthyear, age, name) " +
            "values (1960, 13, 'both')");
        assertResultSet(
            new String[] {
                " null, 'no-age' ", 
                " null, 'no-year' ", 
                " 1973, 'both' " 
            },
            query("select birthyear + age, name from names"));
    }

    public void testMath() throws Exception {
        execute("create table foo (a integer, b integer, c integer)");
        execute("insert into foo(a, b, c) values (4, 5, 6)");
        execute("insert into foo(a, b, c) values (1, 2, 9)");
        ResultSet results = query("select 2 * a + c / 3 - 1 from foo");
        assertResultSet(new String[] { " 9 ", " 4 " }, results);
    }
    
    public void testSearchedCase() throws Exception {
        execute("create table foo(a integer, b integer, c integer)");
        execute("insert into foo(a,b,c) values(5,6,7)");
        execute("insert into foo(a,b,c) values(5,6,null)");
        execute("insert into foo(a,b,c) values(null,6,99)");
        
        // The when clauses should be evaluated in order
        execute("insert into foo(a,b,c) values(null,53,null)");
        
        assertResultSet(
            new String[] { " 11 ", " 18 ", " 6 ", " 53 " },
            query("select case " +
                "when a is null then b " +
                "when c is null then a + b " +
                "else a + b + c " +
                "end " +
                "from foo")
        );
    }
    
    public void testSearchedCaseNoElse() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(5)");
        execute("insert into foo(a) values(6)");
        
        String sql = 
            "select case " +
            "when a = 5 then 55 " +
            "end " +
            "from foo";
        if (dialect.caseExpressionPickyAboutTypes()) {
            expectQueryFailure(sql, 
                "types INTEGER and CHAR are not compatible");
        }
        else {
            assertResultSet(
                new String[] { " 55 ", " null " },
                query(sql)
            );
        }
    }
    
    // TODO: simple case
    // e.g. case a when 7 then 9 when 6 then 5 end
    
}
