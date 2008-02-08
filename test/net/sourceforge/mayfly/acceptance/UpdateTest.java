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
    
    public void testViolateNotNull() throws Exception {
        execute("create table foo (a integer not null)");
        execute("insert into foo(a) values (7)");
        String setToNull = "update foo set a = null";
        // This kind of goes beyond "not null implies defaults",
        // but it is also a MySQL quirk: sometimes null doesn't
        // mean null.
        if (!dialect.notNullImpliesDefaults()) {
            expectExecuteFailure(
                setToNull, "column a cannot be null");
            assertResultSet(new String[] { " 7 " }, query("select a from foo"));
        }
        else {
            execute(setToNull);
            assertResultSet(new String[] { " 0 " }, query("select a from foo"));
        }
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
    
    public void testBadTableNoRows() throws Exception {
        execute("create table foo(a integer)");
        String sql = "update foo set a = 5 where xyz.a = 5";
        if (dialect.errorIfBadTableAndNoRows()) {
            expectExecuteFailure(sql, "no column xyz.a");
        }
        else {
            execute(sql);
        }
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
        assertResultSet(new String[] { "9, 10", "2, 20", "9, 30" }, 
            query("select a, c from foo"));
    }
    
    public void testWhereIncludesTableName() throws Exception {
        execute("create table foo(a integer, c integer)");
        execute("insert into foo(a, c) values(1, 10)");
        assertEquals(1, execute("update foo set c = 20 where foo.c = 10"));
        assertResultSet(new String[] { "1, 20" }, 
            query("select a, c from foo"));
    }
    
    public void testDefault() throws Exception {
        execute("create table foo (a integer default 5)");
        execute("insert into foo(a) values (7)");
        String updateDefault = "update foo set a = default";
        if (dialect.haveUpdateDefault()) {
            assertEquals(1, execute(updateDefault));
            assertResultSet(new String[] { "5" }, query("select a from foo"));
        }
        else {
            expectExecuteFailure(updateDefault, 
                "default doesn't look like a valid expression to me");
        }
    }
    
    public void testAggregate() throws Exception {
        /* Some versions of Postgres apparently crash - CVE-2006-5540 */
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(10)");
        execute("insert into foo(a) values(20)");
        String setToAggregate = "update foo set a = avg(a)";
        if (dialect.errorIfUpdateToAggregate(true)) {
            expectExecuteFailure(setToAggregate,
                "aggregate avg(a) not valid in UPDATE");
            assertResultSet(new String[] { "10", "20" }, 
                query("select a from foo"));
        }
        else {
            /* The CVE-2006-5540 announcement says the meaning here is 
               "not well defined" so I'm commenting out the assert. */
            execute(setToAggregate);
//            assertResultSet(new String[] { "15", "20" }, 
//                query("select a from foo"));
        }
    }

    public void testAggregateNoRows() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(10)");
        execute("insert into foo(a) values(20)");
        String setToAggregate = "update foo set a = avg(a) where a > 50";
        if (dialect.errorIfUpdateToAggregate(false)) {
            expectExecuteFailure(setToAggregate,
                "aggregate avg(a) not valid in UPDATE");
            assertResultSet(new String[] { "10", "20" }, 
                query("select a from foo"));
        }
        else {
            execute(setToAggregate);
        }
    }

    public void testAggregateInWhere() throws Exception {
        execute("create table foo(a integer)");
        String aggregateInWhere = "update foo set a = 5 where max(a) > 10";
        if (dialect.errorIfAggregateInWhere()) {
            expectExecuteFailure(aggregateInWhere,
                "aggregate max(a) not valid in UPDATE");
            assertResultSet(new String[] { }, 
                query("select a from foo"));
        }
        else {
            execute(aggregateInWhere);
        }
    }
    
    public void testJoin() throws Exception {
        execute("create table foo(a integer, aa varchar(255))");
        execute("create table bar(b integer, bb varchar(255))");
        execute("insert into foo(a, aa) values(5, 'five')");
        execute("insert into foo(a, aa) values(6, 'six')");
        execute("insert into bar(b, bb) values(5, 'cinco')");
        
        String joinedUpdate = 
            "update foo, bar set aa = 'one more than four' " +
                "where a = b and bb = 'cinco'";
        if (dialect.canJoinInUpdate()) {
            execute(joinedUpdate);
            assertResultSet(
                new String[] { " 5, 'one more than four' ", " 6, 'six' "},
                query("select a, aa from foo"));
        }
        else {
            expectExecuteFailure(joinedUpdate, "expected SET but got ','");
        }
    }

    /*
     * Same problem as a join, different solution
     */
    public void testSubselect() throws Exception {
        execute("create table foo(a integer, aa varchar(255))");
        execute("create table bar(b integer, bb varchar(255))");
        execute("insert into foo(a, aa) values(5, 'five')");
        execute("insert into foo(a, aa) values(6, 'six')");
        execute("insert into bar(b, bb) values(5, 'cinco')");
        
        execute("update foo set aa = 'one more than four' " +
            "where a = (select b from bar where bb = 'cinco')");
        assertResultSet(
            new String[] { " 5, 'one more than four' ", " 6, 'six' "},
            query("select a, aa from foo"));
    }

}
