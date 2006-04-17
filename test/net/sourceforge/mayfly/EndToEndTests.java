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
        execute("create table foo (x integer default -5, y integer, z integer default +44000)");
        execute("insert into foo(y) values(0)");
        assertResultSet(new String[] { " -5, 44000 " }, query("select x, z from foo"));
    }

}
