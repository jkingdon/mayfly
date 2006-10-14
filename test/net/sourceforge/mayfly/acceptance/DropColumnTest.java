package net.sourceforge.mayfly.acceptance;

public class DropColumnTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo(a integer, b integer)");
        execute("insert into foo(a, b) values (5, 50)");

        /* The SQL92 grammar seems to require CASCADE or RESTRICT
           after the column name, but ommitting that keyword
           seems common.
         */
        String dropColumn = "alter table foo drop column a";
        if (dialect.haveDropColumn()) {
            execute(dropColumn);
            expectExecuteFailure("insert into foo(a) values (6)", "no column a");
            assertResultSet(new String[] { " 50 " }, query("select * from foo"));
        }
        else {
            expectExecuteFailure(dropColumn, "did not expect DROP");
            assertResultSet(new String[] { " 5, 50 " }, query("select * from foo"));
        }
    }
    
    public void testNonexistent() throws Exception {
        if (!dialect.haveDropColumn()) {
            return;
        }

        execute("create table foo(a integer, b integer)");
        expectExecuteFailure("alter table foo drop column c", "no column c");
    }
    
    public void testNotNull() throws Exception {
        if (!dialect.haveDropColumn()) {
            return;
        }

        execute("create table foo(a integer, b integer not null)");
        execute("alter table foo drop column b");
        execute("insert into foo values (5)");
        assertResultSet(new String[] { " 5 " }, query("select * from foo"));
    }
    
    public void testLastColumn() throws Exception {
        if (!dialect.haveDropColumn()) {
            return;
        }

        execute("create table foo(a integer)");
        String dropLastColumn = "alter table foo drop column a";
        String insertRow = "insert into foo values(5)";
        if (dialect.canDropLastColumn()) {
            execute(dropLastColumn);
            expectExecuteFailure(insertRow, "no column a");
        }
        else {
            expectExecuteFailure(dropLastColumn, 
                "attempt to drop the last column: a");
            execute(insertRow);
            assertResultSet(new String[] { " 5 " }, 
                query("select * from foo"));
        }
    }
    
    public void testSingleColumnPrimaryKey() throws Exception {
        if (!dialect.haveDropColumn()) {
            return;
        }

        execute("create table foo(a integer, b integer, primary key(a) )");
        execute("alter table foo drop column a");
    }
    
    // foreign key references column
    // unique or primary key constraint 
    //   (multi-column) references column
    // CASCADE (means also drop foreign keys or views which reference the column)

}