package net.sourceforge.mayfly.acceptance;


public class StoredProcedureTest extends SqlTestCase {
    
    public void testJavaFunctionNoAlias() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(-7)");
        execute("insert into foo(a) values(5)");
        String query = "select \"java.lang.Math.abs\"(a) from foo";
        if (dialect.callJavaMethodAsStoredProcedure()) {
            assertResultSet(
                new String[] { " 5 ", " 7 " }, 
                query(query));
        }
        else {
            expectQueryFailure(query, "expected FROM but got '('");
        }
    }
    
    public void testWithAlias() throws Exception {
        String createAlias = "create alias sample for " +
            "\"net.sourceforge.mayfly.acceptance.StoredProcedureTest.sampleProcedure\";";
        if (dialect.callJavaMethodAsStoredProcedure()) {
            execute(createAlias);
            execute("create table foo(a integer, b integer)");
            execute("insert into foo(a, b) values(2, 3)");
            execute("insert into foo(a, b) values(20, -1)");
            assertResultSet(
                new String[] { " 13 ", " 401 " }, 
                query("select sample(a, b) from foo"));
        }
        else {
            expectExecuteFailure(createAlias, 
                "expected create command but got alias");
        }
    }
    
    static public int sampleProcedure(int a, int b) {
        return a * a + b * b;
    }
    
    // overloaded method case - hypersonic uses the first one with that name.

}
