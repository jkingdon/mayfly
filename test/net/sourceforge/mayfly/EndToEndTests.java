package net.sourceforge.mayfly;

import net.sourceforge.mayfly.acceptance.MayflyDialect;
import net.sourceforge.mayfly.acceptance.SqlTestCase;

/**
 * Tests that could be acceptance tests, but the behavior seems
 * so unimportant that I don't really feel like testing
 * every other database to see what it does.
 * 
 * However, we want some kind of test (mayfly only), because
 * we want little/no code in mayfly that doesn't have a test.
 * Hence this class of mayfly-only end to end tests.
 */
public class EndToEndTests extends SqlTestCase {
    
    public void testThisIsMayfly() throws Exception {
        assertTrue("When testing a non-mayfly database, please just run the tests in the acceptance package",
            dialect instanceof MayflyDialect);
    }

    public void testUnaryPlus() throws Exception {
        execute("create table foo (x integer default -5, " +
            "y integer, " +
            "z integer default +44000)");
        execute("insert into foo(y) values(0)");
        assertResultSet(new String[] { " -5, 44000 " }, 
            query("select x, z from foo"));
    }
    
    public void testQueryVersusUpdate() throws Exception {
        /* Might be interesting to see what other databases do
           (for example, I think hypersonic 1.8.x will throw
           exceptions such as these but 1.7.x will be lenient). */
        execute("create table foo (x integer)");
        expectExecuteFailure("select x from foo", 
            "SELECT is only available with query, not update");
        expectQueryFailure("insert into foo(x) values(5)", 
            "expected SELECT but got INSERT");
    }
    
    public void testTableType() throws Exception {
        /* MySQL compatibility.  In the future perhaps the type will
           do something like give an error if you specify myisam
           and try to use features it doesn't support, like
           transactions or foreign keys.  For now the type is a noop.  */
        execute("create table countries (id integer) type=innodb");
        execute("create table mixedcase (id integer) type=InnoDB");
        execute("create table cities (id integer) type=myisam");
        expectExecuteFailure("create table cities (id integer) type=DataSinkHole", 
            "unrecognized table type DataSinkHole");
    }

    public void testTimestamp() throws Exception {
        execute("create table foo (x timestamp)");
        try {
            execute("insert into foo(x) values('something')");
            fail();
        } catch (UnimplementedException expected) {
            assertEquals("data type timestamp is not implemented", 
                expected.getMessage());
        }
    }

}
