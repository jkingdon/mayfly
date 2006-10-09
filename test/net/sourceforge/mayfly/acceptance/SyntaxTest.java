package net.sourceforge.mayfly.acceptance;

public class SyntaxTest extends SqlTestCase {
    
    // Should mayfly have a SyntaxException which subclasses SQLException?

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
        String createTableSql = "create table \"join\" (" +
            "\"null\" integer, \"=\" integer, \"\u00a1\" integer)";
        if (dialect.canQuoteIdentifiers()) {
            execute(createTableSql);
            execute("insert into \"join\" (" +
                "\"null\", \"=\", \"\u00a1\") values (3, 5, 7)");
            assertResultSet(new String[] { "7, 5, 3" }, query("select \"\u00a1\", \"=\", \"null\" from \"join\""));
        }
        else {
            expectExecuteFailure(createTableSql, null);
        }
    }
    
    public void testQuotedIdentifiersCaseSensitive() throws Exception {
        // The unquoted one is to be taken as the same as quoted "FOO"
        String createTableSql = "create table foo (foo integer, \"Foo\" integer, \"foo\" integer)";
        if (dialect.canQuoteIdentifiers() && 
            dialect.quotedIdentifiersAreCaseSensitive()) {
            execute(createTableSql);
            execute("insert into foo (\"Foo\", \"foo\", \"FOO\") values (3, 5, 7)");
            assertResultSet(new String[] { "5, 3, 7" }, query("select \"foo\", \"Foo\", foo from foo"));
        }
        else {
            expectExecuteFailure(createTableSql, "duplicate column Foo");
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
    
    public void testNonReservedWords() throws Exception {
        String[] nonReserved = new String[] {
            // I believe this list originates from the SQL92 standard
            "ADA",
            "C", "CATALOG_NAME",
            "CHARACTER_SET_CATALOG", "CHARACTER_SET_NAME",
            "CHARACTER_SET_SCHEMA", "CLASS_ORIGIN", "COBOL", "COLLATION_CATALOG",
            "COLLATION_NAME", "COLLATION_SCHEMA", "COLUMN_NAME", "COMMAND_FUNCTION",
            "COMMITTED",
            "CONDITION_NUMBER", "CONNECTION_NAME", "CONSTRAINT_CATALOG", "CONSTRAINT_NAME",
            "CONSTRAINT_SCHEMA", "CURSOR_NAME",
            "DATA", "DATETIME_INTERVAL_CODE",
            "DATETIME_INTERVAL_PRECISION", "DYNAMIC_FUNCTION",
            "FORTRAN",
            "LENGTH",
            "MESSAGE_LENGTH", "MESSAGE_OCTET_LENGTH", "MESSAGE_TEXT", "MORE", "MUMPS",
            "NAME", "NULLABLE", "NUMBER",
            "PASCAL", "PLI",
            "REPEATABLE", "RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", "RETURNED_SQLSTATE",
            "ROW_COUNT",
            "SCALE", "SCHEMA_NAME", "SERIALIZABLE", "SERVER_NAME", "SUBCLASS_ORIGIN",
            "TABLE_NAME", "TYPE",
            "UNCOMMITTED", "UNNAMED"
        };
        for (int i = 0; i < nonReserved.length; i++) {
            assertNotReserved(nonReserved[i]);
        }
        checkReserved("offset");
        
        // Hypersonic pseudo-user for CREATE SCHEMA
        assertNotReserved("dba");
        
        // Derby reserved words (the manual has a longer list)
        checkReserved("first");
        checkReserved("last");

        // Part of DROP TABLE foo IF EXISTS in Mayfly, Hypersonic, and MySQL
        checkReserved("if");

        checkReserved("generated");
        checkReserved("serial");
        checkReserved("identity");
        checkReserved("auto_increment");

        // Part of ALTER TABLE foo MODIFY COLUMN,
        // which isn't in SQL92
        checkReserved("modify");
    }

    private void checkReserved(String word) throws Exception {
        if (dialect.isReservedWord(word)) {
            assertReserved(word);
        }
        else {
            assertNotReserved(word);
        }
    }

    private void assertNotReserved(String identifier) throws Exception {
        execute("create table foo (" + identifier + " integer)");
        execute("drop table foo");
    }

    private void assertReserved(String keyword) throws Exception {
        expectExecuteFailure("create table foo (" + keyword + " integer)",
            "expected column or table constraint but got " + 
                keyword.toUpperCase()
        );
    }
    
    public void testWrongIdentifierForNonReserved() throws Exception {
        // The current rule is that the wrong identifier just
        // doesn't get consumed, so that if an identifier doesn't
        // end up being legal there, we'll eventually get another
        // syntax error.  Is there some reason this won't work?
        execute("create table foo (x integer)");
        expectExecuteFailure("select x from foo limit 10 ofset 20", 
            "expected end of file but got ofset");
    }
    
    public void testComments() throws Exception {
        execute("create table -- single-line comment\n" +
            "foo (x integer) ");
        String slashStarComment =
            "insert into foo /* C-style \n" +
            "comment */ (x) values (5)";
        if (dialect.haveSlashStarComments()) {
            execute(slashStarComment);
        }
        else {
            expectExecuteFailure(slashStarComment, "unexpected character /");
        }
    }

}
