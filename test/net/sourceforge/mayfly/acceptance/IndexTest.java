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
    
    // TODO: Also consider Hypersonic/Derby syntax: CREATE INDEX, etc

}
