package net.sourceforge.mayfly.acceptance;

public class CheckConstraintTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo (a integer, check(a < 7))");
        execute("insert into foo(a) values(4)");
        String violate = "\n\ninsert into foo(a) values(7)";
        if (dialect.haveCheckConstraints()) {
            expectExecuteFailure(
                violate,
                "cannot insert into foo; check constraint failed", 
                3, 20, 3, 29);
            assertResultSet(new String[] { " 4 " }, query("select a from foo"));
        }
        else {
            execute(violate);
            assertResultSet(new String[] { " 4 ", " 7 " }, 
                query("select a from foo"));
        }
    }

}
