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
    
    /* TODO: Might want to insist that the index be on a NOT NULL
       column the way that MySQL 5.1 does */

    // TODO: Also consider Hypersonic/Derby syntax: CREATE INDEX, etc
    
}
