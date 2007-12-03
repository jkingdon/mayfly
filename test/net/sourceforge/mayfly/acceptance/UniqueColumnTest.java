package net.sourceforge.mayfly.acceptance;


public class UniqueColumnTest extends SqlTestCase {

    public void testBasics() throws Exception {
        execute("create table foo (x integer not null, unique(x))");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("insert into foo(x) values(5)", 
            "unique constraint in table foo, column x: duplicate value 5");
    }

    public void testNull() throws Exception {
        String sql = "create table foo (x integer, y integer, unique(x))";
        if (dialect.uniqueColumnMayBeNullable()) {
            execute(sql);
        }
        else {
            expectExecuteFailure(sql, "unique column allows null values");
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
        execute("create table foo (x integer not null, y integer not null, unique(x, y))");
        execute("insert into foo(x,y) values(5,10)");
        execute("insert into foo(x,y) values(5,11)");
        execute("insert into foo(x,y) values(7,10)");
        expectExecuteFailure("insert into foo(x,y) values(5,10)", 
            "unique constraint in table foo, columns x,y: duplicate values 5,10");
    }
    
    public void testUpdate() throws Exception {
        execute("create table foo (x integer not null, unique(x))");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        expectExecuteFailure("update foo set x = 5 where x = 7", 
            "unique constraint in table foo, column x: duplicate value 5");
        expectExecuteFailure("update foo set x = 7 where x = 5", 
            "unique constraint in table foo, column x: duplicate value 7");
        execute("update foo set x = 7 where x = 7");
    }

    public void testAsPartOfDeclaration() throws Exception {
        String partOfColumnDeclaration = "create table foo (x integer not null unique)";
        if (dialect.allowUniqueAsPartOfColumnDeclaration()) {
            execute(partOfColumnDeclaration);
            execute("insert into foo(x) values(5)");
            execute("insert into foo(x) values(7)");
            expectExecuteFailure("insert into foo(x) values(5)", 
                "unique constraint in table foo, column x: duplicate value 5");
        }
        else {
            expectExecuteFailure(partOfColumnDeclaration, "unexpected token UNIQUE");
        }
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
            "unique constraint in table three, column y: duplicate value 10");
        expectExecuteFailure("insert into three(x, y, z) values (3, 13, 100)", 
            "unique constraint in table three, column z: duplicate value 100");
    }
    
    public void testDuplicateConstraintName() throws Exception {
        execute("create table one(x integer primary key)" +
            dialect.tableTypeForForeignKeys());
        String duplicate = "create table foo(x integer not null, y integer, " +
            "constraint foo_x unique(x)," +
            "constraint foo_x foreign key(x) references one(x))" +
            dialect.tableTypeForForeignKeys();
        if (dialect.duplicateConstraintNamesOk()) {
            execute(duplicate);
        }
        else {
            expectExecuteFailure(duplicate, "duplicate constraint name foo_x");
        }
    }

}
