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

    public void testWhereErrorsNoRows() throws Exception {
        execute("create table foo (a integer)");
        execute("create table bar (a integer)");

        String completelyUnknown = "delete from foo where xyz.a = 5";
        String notMentionedInThisCommand = "delete from foo where bar.a = 5";
        if (dialect.errorIfBadTableInDeleteAndNoRows()) {
            expectExecuteFailure(completelyUnknown, "no column xyz.a");
            expectExecuteFailure(notMentionedInThisCommand, "no column bar.a");
        }
        else {
            execute(completelyUnknown);
            execute(notMentionedInThisCommand);
        }
    }

    public void testWhereErrorsWithRows() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo(a) values(5)");
        execute("create table bar (a integer)");
        expectExecuteFailure("delete from foo where xyz.a = 5", 
            "no column xyz.a");
        expectExecuteFailure("delete from foo where bar.a = 5", 
            "no column bar.a");
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
    
    public void testOrderBy() throws Exception {
        execute("create table foo(id integer primary key," +
            "name varchar(255)," +
            "parent integer," +
            "foreign key(parent) references foo(id)" +
            ")" +
            dialect.tableTypeForForeignKeys());
        execute("insert into foo values(1, 'Eve', null)");
        execute("insert into foo values(10, 'Seth', 1)");
        execute("insert into foo values(101, 'Enos', 10)");
        
        String delete = "delete from foo order by id desc";
        if (dialect.allowOrderByOnDelete()) {
            expectExecuteFailure("delete from foo order by id asc", 
                "foreign key violation: table foo refers to id 1 in foo");
            assertResultSet(new String[] { " 'Eve' ", " 'Seth' ", " 'Enos' " }, 
                query("select name from foo"));

            execute(delete);
            assertResultSet(new String[] { }, query("select name from foo"));
        }
        else {
            expectExecuteFailure(delete, "expected end of file but got ORDER");
        }
    }

    public void testSelfReference() throws Exception {
        execute("create table foo(id integer primary key," +
            "name varchar(255)," +
            "parent integer," +
            "foreign key(parent) references foo(id)" +
            ")" +
            dialect.tableTypeForForeignKeys());
        execute("insert into foo values(1, 'Rock', null)");
        execute("insert into foo values(10, 'Paper', 1)");
        execute("insert into foo values(101, 'Scissors', 10)");
        execute("update foo set parent = 101 where id = 1");
        
        expectExecuteFailure("delete from foo where name = 'Rock'", 
            "foreign key violation: table foo refers to id 1 in foo");
        expectExecuteFailure("delete from foo where name = 'Paper'", 
            "foreign key violation: table foo refers to id 10 in foo");
        expectExecuteFailure("delete from foo where name = 'Scissors'", 
            "foreign key violation: table foo refers to id 101 in foo");

        String deleteAll = "delete from foo";
        if (dialect.deleteAllRowsIsSmartAboutForeignKeys()) {
            execute(deleteAll);
            assertResultSet(new String[] { }, query("select name from foo"));
        }
        else {
            expectExecuteFailure("delete from foo where name = 'Rock'", 
                "foreign key violation: table foo refers to id 1 in foo");
        }
    }

}
