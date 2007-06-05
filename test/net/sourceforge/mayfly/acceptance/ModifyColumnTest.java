package net.sourceforge.mayfly.acceptance;

public class ModifyColumnTest extends SqlTestCase {
    
    public void testRemoveNotNull() throws Exception {
        execute("create table foo(a varchar(50) not null)");
        execute("insert into foo(a) values('one')");
        String modify = "alter table foo modify column a varchar(50)";
        if (dialect.haveModifyColumn()) {
            execute(modify);
            execute("insert into foo(a) values(null)");
            assertResultSet(
                new String[] { " 'one' ", " null " }, 
                query("select a from foo"));
        }
        else {
            expectExecuteFailure(modify, 
                "expected alter table action but got MODIFY");
        }
    }
    
    public void testAddNotNull() throws Exception {
        if (!dialect.haveModifyColumn()) {
            return;
        }

        execute("create table foo(a varchar(50))");
        execute("insert into foo(a) values(null)");
        String addNotNullConstraint = 
            "alter table foo modify column a varchar(50) not null";
        expectExecuteFailure(
            addNotNullConstraint,
            "cannot make column a NOT NULL because it contains null values");
        execute("update foo set a = 'one'");
        execute(addNotNullConstraint);
        execute("insert into foo(a) values('two')");
        assertResultSet(
            new String[] { " 'one' ", " 'two' " }, 
            query("select a from foo"));
    }
    
    public void testNonexistentTable() throws Exception {
        if (!dialect.haveModifyColumn()) {
            return;
        }

        expectExecuteFailure(
            "alter table nosuch modify column a integer",
            "no table nosuch");
    }
    
    public void testNonexistentColumn() throws Exception {
        if (!dialect.haveModifyColumn()) {
            return;
        }

        execute("create table foo(x integer)");
        expectExecuteFailure(
            "alter table foo modify column a integer",
            "no column a");
    }
    
    public void testAddAutoIncrement() throws Exception {
        if (!dialect.haveModifyColumn()) {
            return;
        }

        execute("create table foo(x integer, y varchar(50))");
        execute("insert into foo(x, y) values(1, 'before')");
        execute("alter table foo modify column " +
            "x " +
            dialect.autoIncrementType());
        execute("insert into foo(y) values('after')");
        assertResultSet(
            new String[] { "1, 'before'", "2, 'after'" }, 
            query("select x, y from foo"));
    }
    
    // change in type (not yet needed!)

}
