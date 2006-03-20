package net.sourceforge.mayfly.acceptance;

public class PrimaryKeyTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo (x integer, primary key(x))");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("insert into foo(x) values(5)", "primary key x already has a value 5");
    }
    
    public void testNull() throws Exception {
        execute("create table foo (x integer, y integer, primary key(x))");
        expectExecuteFailure("insert into foo(x,y) values(null,10)", "primary key x cannot be null");
    }
    
    public void testManyColumns() throws Exception {
        execute("create table foo (x integer, y integer, primary key(x, y))");
        execute("insert into foo(x,y) values(5,10)");
        execute("insert into foo(x,y) values(5,11)");
        execute("insert into foo(x,y) values(7,10)");
        expectExecuteFailure("insert into foo(x,y) values(5,10)", "primary key x,y already has a value 5,10");
    }
    
    public void testForwardReference() throws Exception {
        String sql = "create table foo (primary key(x), x integer)";
        if (dialect.constraintCanHaveForwardReference()) {
            execute(sql);
            execute("insert into foo(x) values(5)");
            expectExecuteFailure("insert into foo(x) values(5)", "primary key x already has a value 5");
        }
        else {
            expectExecuteFailure(sql, "no column x");
        }
    }
    
    public void testBadReference() throws Exception {
        expectExecuteFailure("create table foo (x integer, primary key(y))", "no column y");
    }

    public void testUpdate() throws Exception {
        if (dialect.updateMissing()) {
            return;
        }
        execute("create table foo (x integer, primary key(x))");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("update foo set x = 5 where x = 7", "primary key x already has a value 5");
    }
    
    public void testAsPartOfDeclaration() throws Exception {
        execute("create table foo (x integer primary key)");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("insert into foo(x) values(5)", "primary key x already has a value 5");
    }
    
}
