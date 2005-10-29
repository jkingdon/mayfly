package net.sourceforge.mayfly;

import junit.framework.*;

import java.sql.*;

public abstract class SqlTestCase extends TestCase {

    protected static final boolean CONNECT_TO_MAYFLY = true;

    private Database database;
    protected Connection connection;
    private Statement statement;

    public void setUp() throws Exception {
        if (CONNECT_TO_MAYFLY) {
            database = new Database();
            connection = database.openConnection();
        } else {
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection("jdbc:hsqldb:mem:SqlTestCase");
        }
    }

    public void tearDown() throws Exception {
        if (!CONNECT_TO_MAYFLY) {
            execute("SHUTDOWN"); // So next test gets a new database.
        }

        if (statement != null) {
            statement.close();
        }
        connection.close();
    }

    protected int execute(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        int rowsAffected = statement.executeUpdate(sql);
        statement.close();
        return rowsAffected;
    }

    protected ResultSet query(String sql) throws SQLException {
        if (statement != null) {
            statement.close();
        }
        statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    protected void assertTableCount(int expected) {
        if (CONNECT_TO_MAYFLY) {
            assertEquals(expected, database.tables().size());
        } else {
            // Could probably do this with JDBC metadata or database-specific tricks.
            // Not clear we should bother.
        }
    }

    protected void assertMessage(String expectedMessage, SQLException exception) {
        if (CONNECT_TO_MAYFLY) {
            assertEquals(expectedMessage, exception.getMessage());
        } else {
            // To assert on this we'd need to keep lists of messages for many
            // databases in many versions.  That seems hard.
            // But we would like to see that databases fail for the same
            // reasons.  So we provide the ability to manually inspect
            // the messages side by side.
            if (false) {
                System.out.print("Mayfly message would be " + expectedMessage + "\n");
                System.out.print("Actual message was " + exception.getMessage() + "\n\n");
            }
        }
    }
    
}
