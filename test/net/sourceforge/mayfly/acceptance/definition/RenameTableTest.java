package net.sourceforge.mayfly.acceptance.definition;

import net.sourceforge.mayfly.acceptance.SqlTestCase;

public class RenameTableTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo(x integer)");
        String rename = "alter table foo rename to bar";
        if (dialect.haveAlterTableRenameTo()) {
            execute(rename);
            assertResultSet(new String[] { }, query("select x from bar"));
            expectQueryFailure("select x from foo", "no table foo");
        }
        else {
            expectExecuteFailure(rename, 
                "expected alter table action but got rename");
        }
    }
    
    public void testDestinationTableAlreadyExists() throws Exception {
        if (!dialect.haveAlterTableRenameTo()) {
            return;
        }
        execute("create table foo(x integer)");
        execute("create table bar(y integer)");
        expectExecuteFailure("alter table foo rename to bar", 
            "table bar already exists; cannot rename foo to bar");
    }
    
    public void testSourceTableDoesNotExist() throws Exception {
        if (!dialect.haveAlterTableRenameTo()) {
            return;
        }
        expectExecuteFailure("alter table foo rename to bar", 
            "no table foo");
    }

    public void testDataIsPreserved() throws Exception {
        if (!dialect.haveAlterTableRenameTo()) {
            return;
        }
        execute("create table foo(x integer)");
        execute("insert into foo(x) values(77)");
        execute("alter table foo rename to bar");
        assertResultSet(new String[] { " 77 " }, query("select x from bar"));
    }
    
    // TODO: constraints pointing to renamed table
    // TODO: constraints pointing from renamed table
    // TODO: error handling if the from table doesn't exist.
    // TODO: rename across schemas.  MySQL sometimes supports this, I think.
    //   It somehow feels wrong.
    // TODO: worry about rowsAffected?  Always 0, I would think.

}
