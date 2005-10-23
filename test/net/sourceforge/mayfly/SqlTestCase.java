package net.sourceforge.mayfly;

import junit.framework.*;

import java.sql.*;

public abstract class SqlTestCase extends TestCase {

    private static final boolean CONNECT_TO_MAYFLY = true;

    private Database database;
    protected Connection connection;

    public void setUp() throws Exception {
        if (CONNECT_TO_MAYFLY) {
            database = new Database();
            connection = database.openConnection();
        } else {
            Class.forName("org.hsqldb.jdbcDriver");
            connection =  DriverManager.getConnection("jdbc:hsqldb:.");
        }
    }

    public void tearDown() throws Exception {
        connection.close();
    }

    protected void execute(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    protected void assertTableCount(int expected) {
        if (CONNECT_TO_MAYFLY) {
            assertEquals(expected, database.tables().size());
        } else {
            // Could probably do this with JDBC metadata or database-specific tricks.
            // Not clear we should bother.
        }
    }
    
}
