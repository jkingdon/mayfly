package net.sourceforge.mayfly.acceptance;

public class AddColumnTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo(a integer)");
        assertEquals(0, execute("alter table foo add column b integer"));
        execute("insert into foo(b, a) values(7,5)");
        assertResultSet(new String[] { " 5, 7 " }, query("select a, b from foo"));
    }
    
    public void testColumnAlreadyExists() throws Exception {
        execute("create table foo(a integer)");
        expectExecuteFailure("alter table foo add column a integer", "column a already exists");
    }
    
    public void testExistingRow() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(5)");
        execute("insert into foo(a) values(6)");

        assertEquals(
            dialect.addingColumnCountsAsAffectedRow() ? 2 : 0, 
            execute("alter table foo add column b integer"));

        assertResultSet(new String[] { " 5, null ", " 6, null " }, 
            query("select a, b from foo"));
    }
    
    public void testNotNull() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(5)");

        String noDefault = "alter table foo add column b integer not null";
        if (dialect.notNullImpliesDefaults()) {
            execute(noDefault);
            assertResultSet(new String[] { " 5, 0 " }, query("select a, b from foo"));
        }
        else {
            expectExecuteFailure(noDefault,
                // "violation of not null constraint" or something might also be OK
                "no default value for column b");
            execute("alter table foo add column b integer default 7 not null");
            assertResultSet(new String[] { " 5, 7 " }, query("select a, b from foo"));
        }
    }
    
    public void testNotNullButNoRows() throws Exception {
        execute("create table foo(a integer)");
        String noDefault = "alter table foo add column b integer not null";
        if (dialect.notNullRequiresDefault()) {
            expectExecuteFailure(noDefault, "no default value for column b");
        }
        else {
            execute(noDefault);
            execute("insert into foo(a, b) values (5, 7)");
        }
    }

}
