package net.sourceforge.mayfly.acceptance;

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
       column the way that MySQL 5.1 does */

    public void testCreateIndexSyntax() throws Exception {
        execute("create table foo(a integer)");
        assertEquals(0, 
            execute("create index an_index_name on foo(a)"));
    }
    
    public void testTwoColumns() throws Exception {
        execute("create table foo(a integer, b integer, c integer)");
        execute("create index an_index_name on foo(c, b)");
    }
    
    public void testUnique() throws Exception {
        execute("create table foo(a integer, b varchar(80))");
        execute("create unique index an_index_name on foo(a)");

        // Also acts as a constraint
        execute("insert into foo(a, b) values(4, 'one')");
        expectExecuteFailure("insert into foo(a, b) values(4, 'two')", 
            "unique column a already has a value 4");
    }
    
    public void testUniqueAndExistingRows() throws Exception {
        execute("create table foo(a integer, b varchar(80))");
        execute("insert into foo(a, b) values(4, 'one')");
        execute("insert into foo(a, b) values(4, 'two')");
        expectExecuteFailure(
            "create unique index an_index_name on foo(a)",
            "unique column a already has a value 4");
        execute("create index an_index_name on foo(a)");
    }
    
    public void testDuplicateName() throws Exception {
        execute("create table foo(f integer)");
        execute("create table bar(b integer)");
        execute("create index an_index_name on foo(f)");
        String duplicate = "create index an_index_name on bar(b)";
        if (dialect.duplicateIndexNamesOk()) {
            execute(duplicate);
        }
        else {
            expectExecuteFailure(duplicate, 
                "index an_index_name already exists");
        }
    }
    
    // TODO: SqlDumper too

}
