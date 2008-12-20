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
    
    public void testForeignKeyFromRenamedTable() throws Exception {
        if (!dialect.haveAlterTableRenameTo()) {
            return;
        }
        execute("create table authors(id integer primary key)" + 
            dialect.tableTypeForForeignKeys());
        execute("create table cookbooks(name varchar(255), " +
        		"author_id integer," +
        		"foreign key(author_id) references authors(id))" + 
                dialect.tableTypeForForeignKeys());
        execute("alter table cookbooks rename to books");

        execute("insert into authors(id) values(5)");
        expectExecuteFailure(
            "insert into books(name, author_id) values ('dal makhani', 77)",
            "foreign key violation: " +
            "attempt in table books, column author_id " +
            "to reference non-present value 77 in " +
            "table authors, column id");
        execute("insert into books(name, author_id) values ('aloo gobi', 5)");
    }
    
    public void testForeignKeyToRenamedTable() throws Exception {
        if (!dialect.haveAlterTableRenameTo()) {
            return;
        }
        execute("create table authors(id integer primary key)" + 
            dialect.tableTypeForForeignKeys());
        execute("create table cookbooks(name varchar(255), " +
                "author_id integer," +
                "foreign key(author_id) references authors(id))" + 
                dialect.tableTypeForForeignKeys());
        execute("alter table authors rename to people");
        
        execute("insert into people(id) values(5)");
        expectExecuteFailure(
            "insert into cookbooks(name, author_id) values ('dal makhani', 77)",
            "foreign key violation: " +
            "attempt in table cookbooks, column author_id " +
            "to reference non-present value 77 in " +
            "table people, column id");
        execute("insert into cookbooks(name, author_id) values ('aloo gobi', 5)");
    }
    
    // TODO: foreign keys pointing to renamed table
    // TODO: CheckConstraint has a table name in it.
    // TODO: error handling if the from table doesn't exist.
    // TODO: rename across schemas.  MySQL sometimes supports this, I think.
    //   It somehow feels wrong.
    // TODO: worry about rowsAffected?  Always 0, I would think.

}
