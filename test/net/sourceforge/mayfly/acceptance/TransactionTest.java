package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTest extends SqlTestCase {
    
    public void testAutoCommitDefaultsToTrue() throws Exception {
        assertEquals(true, connection.getAutoCommit());
    }

    public void testCommit() throws Exception {
        connection.setAutoCommit(false);
        execute("create table foo (x integer)");
        execute("insert into foo(x) values(5)");
        connection.commit();
        assertResultSet(new String[] { " 5 " }, query("select x from foo"));
        
        cleanUpForDerby();
    }

    public void testAutoCommitIsPerConnection() throws Exception {
        Connection connection2 = dialect.openAdditionalConnection();
        try {
            connection2.setAutoCommit(false);
            assertEquals(false, connection2.getAutoCommit());
            assertEquals(true, connection.getAutoCommit());
        }
        finally {
            connection2.close();
        }
    }
    
    public void testRollback() throws Exception {
        // Still need to deal with MySQL, Postgres and Derby
        if (!dialect.haveTransactions()) {
            return;
        }

        execute("create table foo (x integer)");
        connection.setAutoCommit(false);
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        assertResultSet(new String[] { " 5 ", " 7 " }, query("select x from foo"));
        connection.rollback();
        // This will the symptom if using MySQL without InnoDB:
//        assertResultSet(new String[] { " 5 ", " 7 " }, query("select x from foo"));
        assertResultSet(new String[] { }, query("select x from foo"));
        
        cleanUpForDerby();
    }
    
    public void testRollbackAndAutoCommit() throws Exception {
        if (!dialect.haveTransactions()) {
            return;
        }

        execute("create table foo (x integer)");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        assertResultSet(new String[] { " 5 ", " 7 " }, query("select x from foo"));
        // MySQL insists on auto-commit false in order to call rollback.
        // That might be worth insisting on, if apps can deal.
//        connection.setAutoCommit(false);
        connection.rollback();
        assertResultSet(new String[] { " 5 ", " 7 " }, query("select x from foo"));
    }
    
    private void cleanUpForDerby() throws SQLException {
        // Derby needs this before the close.
        // At some point, think about whether we should test for
        // whether that is needed.
        connection.setAutoCommit(true);
    }
    
    // Need to deal with whether SET SCHEMA is transactional (see Derby docs)
    
    // setAutoCommit(true) with a transaction in progress
    
    // commit or rollback with a result set, prepared statement, etc open
    // (this is mentioned in Derby docs, I think)
    
    // two connections - uncommitted insert seen/not-seen by other
    // two connections - uncommitted update seen/not-seen by other
    // uncommitted create table seen/not-seen by other
    // uncommitted create schema seen/not-seen by other

    /* transaction 1 reads (or just starts a transaction? what triggers start?)
     * transaction 2 updates a row that 1 has already read
     * transaction 1 re-reads
     * So is the update seen/not-seen
     * ("repeatable read" property)
     */

    /* transaction 1 reads
     * transaction 2 inserts a row
     * transaction 1 re-reads
     * So is the insert seen/not-seen
     * ("phantom read" property)
     */

}
