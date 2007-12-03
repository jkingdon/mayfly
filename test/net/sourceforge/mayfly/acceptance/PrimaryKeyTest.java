package net.sourceforge.mayfly.acceptance;

public class PrimaryKeyTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo (x integer, primary key(x))");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("insert into foo(x) values(5)", 
            "primary key in table foo, column x: duplicate value 5");
    }
    
    public void testNull() throws Exception {
        execute("create table foo (x integer, y integer, primary key(x))");
        expectExecuteFailure("insert into foo(x,y) values(null,10)", 
            dialect.wishThisWereTrue() ?
                "primary key in table foo, column x: cannot be null" :
                "primary key x cannot be null");
    }
    
    public void testManyColumns() throws Exception {
        execute("create table foo (x integer, y integer, primary key(x, y))");
        execute("insert into foo(x,y) values(5,10)");
        execute("insert into foo(x,y) values(5,11)");
        execute("insert into foo(x,y) values(7,10)");
        expectExecuteFailure("insert into foo(x,y) values(5,10)", 
            "primary key in table foo, columns x,y: duplicate values 5,10");
    }
    
    public void testForwardReference() throws Exception {
        String sql = "create table foo (primary key(x), x integer)";
        if (dialect.constraintCanHaveForwardReference()) {
            execute(sql);
            execute("insert into foo(x) values(5)");
            expectExecuteFailure("insert into foo(x) values(5)", 
                "primary key in table foo, column x: duplicate value 5");
        }
        else {
            expectExecuteFailure(sql, "no column x");
        }
    }
    
    public void testBadReference() throws Exception {
        expectExecuteFailure("create table foo (x integer, primary key(y))", "no column y");
    }

    public void testUpdate() throws Exception {
        execute("create table foo (x integer, primary key(x))");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("update foo set x = 5 where x = 7", 
            "primary key in table foo, column x: duplicate value 5");
    }
    
    public void testAsPartOfDeclaration() throws Exception {
        execute("create table foo (x integer primary key)");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("insert into foo(x) values(5)", 
            "primary key in table foo, column x: duplicate value 5");
    }
    
    public void testTwoPrimaryKeys() throws Exception {
        expectExecuteFailure(
            "create table foo (x integer primary key, y integer, primary key(y))",
            "attempt to define more than one primary key for table foo");
    }
    
    public void testNamedConstraint() throws Exception {
        // TODO: the case of "constraint mars.my_primary_key" (with schema)
        execute("create table foo (x integer, " +
            "constraint my_primary_key primary key(x)" +
            ")");
        
        execute("insert into foo(x) values(5)");
        expectExecuteFailure("insert into foo(x) values(5)", 
            dialect.wishThisWereTrue() ?
                "primary key my_primary_key (table foo, column x): duplicate value 5" :
                "primary key in table foo, column x: duplicate value 5");
    }
    
    public void testNotNull() throws Exception {
        // Not a primary key, but related enough to put in the same test file
        execute("create table foo (x integer not null)");
        expectExecuteFailure("insert into foo(x) values(null)",
            dialect.wishThisWereTrue() ?
                "violation of not-null constraint: table foo, column x" :
                "column x cannot be null", 
            1, 27, 1, 31);
    }
    
}
