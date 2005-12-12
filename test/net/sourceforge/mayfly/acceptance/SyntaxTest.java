package net.sourceforge.mayfly.acceptance;



public class SyntaxTest extends SqlTestCase {
    
    // Should we have a SyntaxException which subclasses SQLException?

    public void testBadCommand() throws Exception {
        expectExecuteFailure("PICK NOSE", "unexpected token: PICK");
    }

    public void testCommandsAreCaseInsensitive() throws Exception {
        expectExecuteFailure("DrOp tAbLe FOo", "no table FOo");
    }

    public void testColumnsMissingOnCreate() throws Exception {
        // Really should try to do better than "unexpected token: null".
        // It comes from ANTLR; what is the right fix?
//        expectExecuteFailure("create table foo", "must specify columns on create");
        expectExecuteFailure("create table foo", "unexpected token: null");

        //expectExecuteFailure("create table foo (a integer", "didn't see expected token )");
        expectExecuteFailure("create table foo (a integer", "unexpected token: null");
    }
    
    // Apparently neither ldbc nor jsqlparser can quote identifiers
    // at all.  Surprising.
    public void xtestXAndQuoting() throws Exception {
        execute("create table \"foo\" (\"x\" integer)");
        //query("select x from foo");
        query("select \"x\" from \"foo\"");
        //query("select \"x\" from foo"); // not legal, at least in hypersonic
    }
    
    public void testTableNamesCaseInsensitive() throws Exception {
        if (dialect.tableNamesMightBeCaseSensitive()) {
            return;
        }

        execute("create table foo (x integer)");
        execute("insert into Foo (X) values (5)");
        assertResultSet(new String[] { " 5 " } , query("select x from FOO"));
    }

}
