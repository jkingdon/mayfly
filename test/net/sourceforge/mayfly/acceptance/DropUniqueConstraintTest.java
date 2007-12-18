package net.sourceforge.mayfly.acceptance;


public class DropUniqueConstraintTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo(x integer not null," +
            "y varchar(255)," +
            "constraint x_uniq unique(x)" +
            ")");
        execute("insert into foo(x, y) values(5, 'first')");
        String insertSecond = "insert into foo(x, y) values(5, 'second')";
        expectExecuteFailure(insertSecond, 
            "unique constraint in table foo, column x: duplicate value 5");
        
        String dropConstraint = "alter table foo drop constraint x_uniq";
        if (dialect.haveDropConstraint()) {
            execute(dropConstraint);
            execute(insertSecond);
            assertResultSet(
                 new String[] { " 5, 'first' ", " 5 , 'second' "}, 
                 query("select x, y from foo"));
        }
        else {
            expectExecuteFailure(dropConstraint, 
                "expected alter table drop action but got CONSTRAINT");
        }
    }

}
