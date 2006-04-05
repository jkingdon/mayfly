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
        execute("create table foo (x integer not null, unique(x))");
        execute("insert into foo(x) values(5)");
        expectExecuteFailure("insert into foo(x) values(5)", "unique column x already has a value 5");
    }
    
    public void testMultipleConstraints() throws Exception {
        /** Must be after DEFAULT but constraints can be in any order
            Not that UNIQUE and PRIMARY KEY together on a column make much semantic sense,
            which is why we aren't testing it here.
            See {@link net.sourceforge.mayfly.parser.ParserTest#testMultipleConstraints()}.
         */

        execute("create table three (x integer, y integer not null, z integer not null," +
            " unique(y), primary key(x), unique(z))");
        
        execute("insert into three(x, y, z) values (0, 10, 100)");
        expectExecuteFailure("insert into three(x, y, z) values (null, 11, 101)", 
            "primary key x cannot be null");
        expectExecuteFailure("insert into three(x, y, z) values (2, 10, 102)", 
            "unique column y already has a value 10");
        expectExecuteFailure("insert into three(x, y, z) values (3, 13, 100)", 
            "unique column z already has a value 100");
    }

    private boolean createTable(String sql) throws SQLException {
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
