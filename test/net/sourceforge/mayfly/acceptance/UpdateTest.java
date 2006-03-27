package net.sourceforge.mayfly.acceptance;

public class UpdateTest extends SqlTestCase {
    
    public void testNoRows() throws Exception {
        execute("create table foo (a integer)");
        assertEquals(0, execute("update foo set a = 5"));
        assertResultSet(new String[] { }, query("select a from foo"));
    }
    
    public void testTwoColumns() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo(a) values (null)");
        assertEquals(1, execute("update foo set b = 6, a = 5"));
        assertResultSet(new String[] { " 5, 6 " }, query("select a, b from foo"));
    }
    
    public void testSetNull() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo(a) values (7)");
        assertEquals(1, execute("update foo set a = null"));
        assertResultSet(new String[] { " null " }, query("select a from foo"));
    }
    
    public void testCaseInsensitive() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo(a) values (7)");
        assertEquals(1, execute("update foo set A = 8"));
        assertResultSet(new String[] { " 8 " }, query("select a from foo"));
    }
    
    public void testBadColumnName() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo(a) values (7)");
        expectExecuteFailure("update foo set b = 8", "no column b");
        assertResultSet(new String[] { " 7 " }, query("select a from foo"));
    }
    
    public void testExpression() throws Exception {
        execute("create table foo (a integer, offset_value integer)");
        execute("insert into foo(a, offset_value) values (2, 1000)");
        execute("insert into foo(a, offset_value) values (3, 2000)");
        execute("insert into foo(a, offset_value) values (4, 3000)");
        assertEquals(3, execute("update foo set a = a * a + offset_value"));
        assertResultSet(new String[] { " 1004 ", "2009", "3016" }, query("select a from foo"));
    }
    
    public void testWhere() throws Exception {
        execute("create table foo (a integer, b varchar(255), c integer)");
        execute("insert into foo(a, b, c) values (1, 'set-me', 10)");
        execute("insert into foo(a, b, c) values (2, 'do-not-set-me', 20)");
        execute("insert into foo(a, b, c) values (3, 'set-me', 30)");
        assertEquals(2, execute("update foo set a = 9 where b = 'set-me'"));
        assertResultSet(new String[] { "9, 10", "2, 20", "9, 30" }, query("select a, c from foo"));
    }
    
    public void testDefault() throws Exception {
        if (!dialect.wishThisWereTrue()) {
            return;
        }
        execute("create table foo (a integer default 5)");
        execute("insert into foo(a) values (7)");
        String updateDefault = "update foo set a = default";
        if (dialect.haveUpdateDefault()) {
            assertEquals(1, execute(updateDefault));
            assertResultSet(new String[] { "5" }, query("select a from foo"));
        }
        else {
            expectExecuteFailure(updateDefault, "default doesn't look like a valid expression to me");
        }
    }

}
