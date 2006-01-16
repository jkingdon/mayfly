package net.sourceforge.mayfly.acceptance;

public class SyntaxTest extends SqlTestCase {
    
    // Should we have a SyntaxException which subclasses SQLException?

    public void testBadCommand() throws Exception {
        expectExecuteFailure("PICK NOSE", "expected command but got PICK");
    }

    public void testCommandsAreCaseInsensitive() throws Exception {
        expectExecuteFailure("DrOp tAbLe FOo", "no table FOo");
    }

    public void testColumnsMissingOnCreate() throws Exception {
        // Is "must specify columns" any better?
        // "expected '('" might be about as good.
//        expectExecuteFailure("create table foo", "must specify columns on create");
        expectExecuteFailure("create table foo", "expected '(' but got end of file");

        expectExecuteFailure("create table foo (a integer", "expected ')' but got end of file");
    }
    
    public void testQuotedIdentifier() throws Exception {
        String createTableSql = "create table \"join\" (\"null\" integer, \"=\" integer, \"\u00a1\" integer)";
        if (dialect.canQuoteIdentifiers()) {
            execute(createTableSql);
            execute("insert into \"join\" (\"null\", \"=\", \"\u00a1\") values (3, 5, 7)");
            assertResultSet(new String[] { "7, 5, 3" }, query("select \"\u00a1\", \"=\", \"null\" from \"join\""));
        }
        else {
            expectExecuteFailure(createTableSql, null);
        }
    }
    
    public void testTableNamesCaseInsensitive() throws Exception {
        if (dialect.tableNamesMightBeCaseSensitive()) {
            return;
        }

        execute("create table foo (x integer)");
        execute("insert into Foo (X) values (5)");
        assertResultSet(new String[] { " 5 " } , query("select x from FOO"));
        assertResultSet(new String[] { " 5 " } , query("select Foo.x from foo"));
        assertResultSet(new String[] { " 5 " } , query("select x from foo where FOO.x = 5"));
    }

}
