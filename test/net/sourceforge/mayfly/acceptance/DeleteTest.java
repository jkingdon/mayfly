package net.sourceforge.mayfly.acceptance;

public class DeleteTest extends SqlTestCase {
    
    public void testDelete() throws Exception {
        execute("create table foo (a integer, b varchar(255))");
        execute("insert into foo (a, b) values (5, 'Chicago')");
        execute("insert into foo (a, b) values (5, 'New York')");
        execute("insert into foo (a, b) values (7, 'Paris')");
        
        assertEquals(2, execute("delete from foo where a = 5"));
        
        assertResultSet(new String[] { " 'Paris' "}, query("select b from foo"));
    }

    public void testAggregateInWhere() throws Exception {
        execute("create table foo(a integer)");
        String aggregateInWhere = "delete from foo where max(a) > 10";
        if (dialect.errorIfAggregateInWhere()) {
            expectExecuteFailure(aggregateInWhere,
                "aggregate max(a) not valid in DELETE");
            assertResultSet(new String[] { }, 
                query("select a from foo"));
        }
        else {
            execute(aggregateInWhere);
        }
    }

}
