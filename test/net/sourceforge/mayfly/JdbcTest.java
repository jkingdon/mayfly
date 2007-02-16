package net.sourceforge.mayfly;

import junit.framework.TestCase;

import net.sourceforge.mayfly.acceptance.SqlTestCase;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.dump.SqlDumper;
import net.sourceforge.mayfly.jdbc.JdbcConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTest extends TestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
        Class.forName("net.sourceforge.mayfly.JdbcDriver");
    }
    
    protected void tearDown() throws Exception {
        JdbcDriver.shutdown();
        super.tearDown();
    }

    public void testConnectViaDriverManager() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:mayfly:");
        PreparedStatement createTable = connection.prepareStatement("CREATE TABLE FOO (a integer)");
        assertEquals(0, createTable.executeUpdate());
        createTable.close();
        
        PreparedStatement select = connection.prepareStatement("SELECT A FROM FOO");
        ResultSet results = select.executeQuery();
        assertFalse(results.next());
        results.close();
        select.close();
        
        connection.close();
    }
    
    public void testBadJdbcUrl() throws Exception {
        expectedFailedConnection("Mayfly JDBC URL jdbc:mayfly:x not recognized", "jdbc:mayfly:x");
    }
    
    public void testDoNotAcceptNull() throws Exception {
        JdbcDriver driver = new JdbcDriver();
        assertFalse(driver.acceptsURL(null));
    }

    public void testAccessSnapshotViaDriverManager() throws Exception {
        Database original = new Database();
        original.execute("create table foo (a integer)");
        original.execute("insert into foo(a) values(6)");
        DataStore dataStore = original.dataStore();

        String restored1 = JdbcDriver.create(dataStore);
        update(restored1, 1, "insert into foo(a) values(1)");
        query(restored1, new String[] {"1", "6"}, "select a from foo");

        String restored2 = JdbcDriver.create(dataStore);
        update(restored2, 1, "insert into foo(a) values(2)");
        query(restored2, new String[] {"2", "6"}, "select a from foo");
    }
    
    public void testSnapshot() throws Exception {
        String url = JdbcDriver.create(new DataStore());
        update(url, 0, "create table foo(a integer)");
        DataStore store = JdbcDriver.snapshot(url);
        String dump = new SqlDumper().dump(store);
        assertEquals("CREATE TABLE foo(\n  a INTEGER\n);\n\n", dump);
    }
    
    public void testSnapshotOfDefault() throws Exception {
        update("jdbc:mayfly:", 0, "create table foo(b integer)");
        DataStore store = JdbcDriver.snapshot("jdbc:mayfly:");
        String dump = new SqlDumper().dump(store);
        assertEquals("CREATE TABLE foo(\n  b INTEGER\n);\n\n", dump);
    }
    
    public void testSnapshotOfUnrecognized() throws Exception {
        try {
            JdbcDriver.snapshot("jdbc:mayfly:random:thing");
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "Mayfly JDBC URL jdbc:mayfly:random:thing not recognized",
                e.getMessage());
        }
    }

    public void testSnapshotViaConnection() throws Exception {
        String url = JdbcDriver.create(new DataStore());
        update(url, 0, "create table foo(a integer)");
        Connection connection = DriverManager.getConnection(url);
        passToSomeOtherPartOfTheCode(connection);
    }
    
    private void passToSomeOtherPartOfTheCode(Connection connection) {
        JdbcConnection mayflyConnection = (JdbcConnection) connection;
        DataStore store = mayflyConnection.snapshot();
        String dump = new SqlDumper().dump(store);
        assertEquals("CREATE TABLE foo(\n  a INTEGER\n);\n\n", dump);
    }

    public void testShutdown() throws Exception {
        String first = JdbcDriver.create(new Database().dataStore());
        JdbcDriver.shutdown();

        expectedFailedConnection("Mayfly JDBC URL " + first + " not recognized", first);

        String second = JdbcDriver.create(new Database().dataStore());
        expectedFailedConnection("Mayfly JDBC URL " + first + " not recognized", first);
        update(second, 0, "create table foo (a integer)");
    }
    
    public void testConnectionsOpenAtShutdownTime() throws Exception {
        /**
         * For the moment, a Connection contains a reference to the Database
         * it came from, and so will just operate against that old Database.
         * What we really want is that shutdown checks that all connections
         * are closed, and complains if not.  Probably likewise for
         * connections which came from {@link Database#openConnection()}
         * (we'd need a shutdown method in Database which people would need
         * to call).
         * 
         * Oh, and when we complain about a connection which hasn't been
         * closed, we probably want to include a stack trace to where it
         * was opened (or provide a mechanism for getting said stack trace).
         */ 
        update("jdbc:mayfly:", 0, "create table foo (a integer)");
        update("jdbc:mayfly:", 1, "insert into foo (a) values (5)");
        Connection connection = DriverManager.getConnection("jdbc:mayfly:");
        
        JdbcDriver.shutdown();
        
        Statement statement = connection.createStatement();
        SqlTestCase.assertResultSet((new String[] {"5"}), statement.executeQuery("select a from foo"));
        statement.close();
        connection.close();
    }
    
    public void testShutdownAndDefaultUrl() throws Exception {
        update("jdbc:mayfly:", 0, "create table foo (a integer)");
        update("jdbc:mayfly:", 1, "insert into foo (a) values (5)");
        
        query("jdbc:mayfly:", new String[] {"5"}, "select a from foo");

        JdbcDriver.shutdown();
        
        Connection connection = DriverManager.getConnection("jdbc:mayfly:");
        Statement statement = connection.createStatement();
        try {
            statement.executeQuery("select a from foo");
            fail();
        } catch (SQLException e) {
            assertEquals("no table foo", e.getMessage());
        }
        statement.close();
        connection.close();
    }
    
    public void testIsClosed() throws Exception {
        Database database = new Database();
        Connection connection = database.openConnection();
        Connection secondConnection = database.openConnection();
        assertFalse(connection.isClosed());
        connection.close();
        assertTrue(connection.isClosed());
        
        // multiple close calls have no effect
        connection.close();
        assertTrue(connection.isClosed());
        
        assertFalse(secondConnection.isClosed());
    }
    
    public void testOperationsOnClosedConnection() throws Exception {
        Database database = new Database();
        Connection connection = database.openConnection();
        connection.close();

        try {
            connection.createStatement();
            fail();
        }
        catch (SQLException e) {
            assertEquals("connection is closed", e.getMessage());
        }

        try {
            connection.prepareStatement("even syntax errors not detected");
            fail();
        }
        catch (SQLException e) {
            assertEquals("connection is closed", e.getMessage());
        }
    }
    
    public void testFailingCommandViaStatement() throws Exception {
        Database database = new Database();
        database.execute("create table foo(x integer not null)");
        Connection connection = database.openConnection();
        Statement statement = connection.createStatement();
        try {
            statement.executeUpdate("insert into foo(x) values(null)");
            fail();
        }
        catch (MayflySqlException e) {
            assertEquals("column x cannot be null", e.getMessage());
            assertEquals("insert into foo(x) values(null)", e.failingCommand());
        }
    }
    
    public void testFailingCommandViaPreparedStatement() throws Exception {
        Database database = new Database();
        database.execute("create table foo(x integer not null)");
        Connection connection = database.openConnection();
        PreparedStatement statement = connection.prepareStatement(
            "insert into foo(x) values(null)");
        try {
            statement.executeUpdate();
            fail();
        }
        catch (MayflySqlException e) {
            assertEquals("column x cannot be null", e.getMessage());
            assertEquals("insert into foo(x) values(null)", e.failingCommand());
        }
    }
    
    private void query(String jdbcUrl, String[] expectedResults, String sql) 
    throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        Statement statement = connection.createStatement();
        SqlTestCase.assertResultSet(expectedResults, statement.executeQuery(sql));
        statement.close();
        connection.close();
    }

    private void update(String jdbcUrl, int expectedRowsAffected, String sql) 
    throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        Statement statement = connection.createStatement();
        assertEquals(expectedRowsAffected, statement.executeUpdate(sql));
        statement.close();
        connection.close();
    }

    private void expectedFailedConnection(String expectedMessage, String url) {
        try {
            DriverManager.getConnection(url);
            fail();
        } catch (SQLException expected) {
            assertEquals(expectedMessage, expected.getMessage());
        }
    }
    
}
