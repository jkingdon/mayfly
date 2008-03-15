package net.sourceforge.mayfly.acceptance;

import java.sql.SQLException;

public class IndexTest extends SqlTestCase {
    
    public void testMysqlSyntax() throws Exception {
        String sql = "create table foo(a integer, index(a))";
        if (dialect.createTableCanContainIndex()) {
            execute(sql);
            execute("create table bar(b integer, index named_index(b))");
        }
        else {
            expectExecuteFailure(sql, "expected data type but got '('");
        }
    }
    
    public void testMysqlSyntaxTwoColumns() throws Exception {
        String sql = "create table foo(a integer, b integer, index(b, a))";
        if (dialect.createTableCanContainIndex()) {
            execute(sql);
        }
        else {
            expectExecuteFailure(sql, "expected data type but got '('");
        }
    }
    
    /* TODO: Might want to insist that the index be on a NOT NULL
       column the way that MySQL 5.1 does 
       (apparently these tests just pass with MySQL 5.0). */

    public void testCreateIndexSyntax() throws Exception {
        execute("create table foo(a integer)");
        assertEquals(0, 
            execute("create index an_index_name on foo(a)"));
    }
    
    public void testTwoColumns() throws Exception {
        execute("create table foo(a integer, b integer, c integer)");
        execute("create index an_index_name on foo(c, b)");
    }
    
    public void testNotUniqueByDefault() throws Exception {
        execute("create table foo(a integer, b varchar(80))");
        execute("create index an_index_name on foo(a)");

        execute("insert into foo(a, b) values(4, 'one')");
        execute("insert into foo(a, b) values(4, 'two')");
        assertResultSet(new String[] { " 4, 'one' ", " 4, 'two' "}, 
            query("select a, b from foo"));
    }
    
    public void testUnique() throws Exception {
        execute("create table foo(a integer, b varchar(80))");
        execute("create unique index an_index_name on foo(a)");

        // Also acts as a constraint
        execute("insert into foo(a, b) values(4, 'one')");
        expectExecuteFailure("insert into foo(a, b) values(4, 'two')", 
            "unique constraint in table foo, column a: duplicate value 4");
    }
    
    public void testUniqueAndExistingRows() throws Exception {
        execute("create table foo(a integer, b varchar(80))");
        execute("insert into foo(a, b) values(4, 'one')");
        execute("insert into foo(a, b) values(4, 'two')");
        expectExecuteFailure(
            "create unique index an_index_name on foo(a)",
            "unique constraint in table foo, column a: duplicate value 4");
        execute("create index an_index_name on foo(a)");
    }
    
    public void testDuplicateName() throws Exception {
        execute("create table foo(f integer)");
        execute("create table bar(b integer)");
        execute("create index an_index_name on foo(f)");
        String duplicate = "create index an_index_name on bar(b)";
        if (dialect.indexNamesArePerTable()) {
            execute(duplicate);
        }
        else {
            expectExecuteFailure(duplicate, 
                "index an_index_name already exists");
        }
    }
    
    public void testIndexOnPartOfColumn() throws Exception {
        execute("create table foo(a varchar(255))");
        String sql = "create index my_index on foo(a(10))";
        if (dialect.canIndexPartOfColumn()) {
            execute(sql);
        }
        else {
            expectExecuteFailure(sql, "expected ')' but got '('");
        }
    }
    
    public void testDropIndex() throws Exception {
        execute("create table foo(a integer, b integer)");
        execute("create index an_index on foo(a)");
        String tryToCreateIndexWithSameName = "create index an_index on foo(b)";
        expectExecuteFailure(tryToCreateIndexWithSameName, 
            "duplicate index an_index");

        String dropWithoutGivingTable = "drop index an_index";
        if (dialect.indexNamesArePerTable()) {
            expectExecuteFailure(dropWithoutGivingTable, 
                "expected ON but got end of file");
            execute("drop index an_index on foo");
        }
        else {
            execute(dropWithoutGivingTable);
        }

        execute(tryToCreateIndexWithSameName);
    }
    
    public void testDroppingUniqueIndexDropsConstraint() throws Exception {
        execute("create table foo(a integer, b varchar(20))");
        execute("create unique index an_index on foo(a)");
        dropIndex("an_index");
        dialect.checkDump(
            "CREATE TABLE foo(\n" +
            "  a INTEGER,\n" +
            "  b VARCHAR(20)\n" +
            ");\n\n");
        execute("insert into foo(a, b) values(6, 'first')");
        execute("insert into foo(a, b) values(6, 'second')");
    }
    
    public void testDroppingIndexDoesNotAffectConstraint() throws Exception {
        String createTable = "create table foo(a integer, b varchar(20), unique(a))";
        if (dialect.uniqueColumnMayBeNullable()) {
            execute(createTable);
        }
        else {
            expectExecuteFailure(createTable, 
                "cannot combine nullable column and unique constraint: table foo, column a");
            // If it were just the NOT NULL thing, we could work around it.
            // But I couldn't get the rest of this test to work in Derby.
            return;
        }

        execute("create index an_index on foo(a)");
        dropIndex("an_index");
        dialect.checkDump(
            "CREATE TABLE foo(\n" +
            "  a INTEGER,\n" +
            "  b VARCHAR(20),\n" +
            "  UNIQUE(a)\n" +
            ");\n\n");
        execute("insert into foo(a, b) values(6, 'first')");
        expectExecuteFailure("insert into foo(a, b) values(6, 'second')",
            "unique constraint in table foo, column a: duplicate value 6");
    }

    private void dropIndex(String name) throws SQLException {
        if (dialect.indexNamesArePerTable()) {
            execute("drop index " + name + " on foo");
        }
        else {
            execute("drop index " + name);
        }
    }
    
    public void mysql_only_testAlterTableDropIndex() throws Exception {
        // another syntax, as an alternative to the DROP INDEX one

        execute("create table foo(a integer, b integer)");
        execute("create index an_index on foo(a)");
        String tryToCreateIndexWithSameName = "create index an_index on foo(b)";
        expectExecuteFailure(tryToCreateIndexWithSameName, 
            "duplicate index an_index");

        execute("alter table foo drop index an_index");

        execute(tryToCreateIndexWithSameName);
    }
    
    /* Another case for duplicates is:
        execute("create index an_index on foo(a)");
        execute("create index an_index on foo(a)");
       Derby seems to like that one, but hypersonic and postgres don't.
     */
    
}
