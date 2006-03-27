package net.sourceforge.mayfly.acceptance;

import java.sql.SQLException;

public class UniqueColumnTest extends SqlTestCase {

    public void testBasics() throws Exception {
        if (!createTable("create table foo (x integer, unique(x))")) {
            return;
        }
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("insert into foo(x) values(5)", "unique column x already has a value 5");
    }

    public void testNull() throws Exception {
        if (!createTable("create table foo (x integer, y integer, unique(x))")) {
            return;
        }
        execute("insert into foo(x,y) values(null,10)");
        assertResultSet(new String[] { " null, 10 " }, query("select x,y from foo"));

        String insertSecondNull = "insert into foo(x,y) values(null,11)";
        if (dialect.allowMultipleNullsInUniqueColumn()) {
            execute(insertSecondNull);
            assertResultSet(new String[] { " null, 10 ", " null, 11 " }, query("select x,y from foo"));
        }
        else {
            expectExecuteFailure(insertSecondNull, "unique column x already has a value null");
        }
    }
    
    public void testManyColumns() throws Exception {
        if (!createTable("create table foo (x integer, y integer, unique(x, y))")) {
            return;
        }
        execute("insert into foo(x,y) values(5,10)");
        execute("insert into foo(x,y) values(5,11)");
        execute("insert into foo(x,y) values(7,10)");
        // terminology problem: it isn't one column
        expectExecuteFailure("insert into foo(x,y) values(5,10)", "unique column x,y already has a value 5,10");
    }
    
    public void testUpdate() throws Exception {
        if (!dialect.wishThisWereTrue()) {
            // no updates
            return;
        }
        if (!createTable("create table foo (x integer, unique(x))")) {
            return;
        }
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("update foo set x = 5 where x = 7", "unique column x already has a value 5");
    }
    
    public void testAsPartOfDeclaration() throws Exception {
        String partOfColumnDeclaration = "create table foo (x integer unique)";
        if (dialect.allowUniqueAsPartOfColumnDeclaration()) {
            if (!createTable(partOfColumnDeclaration)) {
                return;
            }
        }
        else {
            expectExecuteFailure(partOfColumnDeclaration, "unexpected token UNIQUE");
            return;
        }
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("insert into foo(x) values(5)", "unique column x already has a value 5");
    }
    
    public void testCombineWithNotNull() throws Exception {
        if (!dialect.wishThisWereTrue()) {
            // no unique constraints
            return;
        }

        execute("create table foo (x integer not null, unique(x))");
        execute("insert into foo(x) values(5)");
        expectExecuteFailure("insert into foo(x) values(5)", "unique column x already has a value 5");
    }

    private boolean createTable(String sql) throws SQLException {
        if (!dialect.wishThisWereTrue()) {
            return false;
        }

        if (dialect.uniqueColumnMayBeNullable()) {
            execute(sql);
            return true;
        }
        else {
            expectExecuteFailure(sql, "unique column allows null values");
            return false;
        }
    }
    
}
