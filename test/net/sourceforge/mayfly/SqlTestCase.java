package net.sourceforge.mayfly;

import junit.framework.*;

import net.sourceforge.mayfly.util.*;

import java.sql.*;
import java.util.*;

public abstract class SqlTestCase extends TestCase {

    private static final boolean CONNECT_TO_MAYFLY = true;

    // Turn this on to see a comparison of mayfly exception messages with
    // the current database's messages.
    private static final boolean SHOW_MESSAGES = false;

    /** Should a test look for behavior in which Mayfly intentionally diverges
     * from what hypersonic does? */
    protected static final boolean EXPECT_MAYFLY_BEHAVIOR = CONNECT_TO_MAYFLY;
    /** Should a test skip checking for behaviors which we plan to implement in Mayfly,
     * but which aren't implemented yet?  */
    protected static final boolean MAYFLY_MISSING = !CONNECT_TO_MAYFLY;

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
            if (SHOW_MESSAGES) {
                System.out.print("Mayfly message would be " + expectedMessage + "\n");
                System.out.print("Actual message was " + exception.getMessage() + "\n\n");
            }
        }
    }

    public static void assertResultSet(String[] rowsAsStrings, ResultSet results) throws SQLException {
        Collection expected = new HashSet();
        HashSet actual = new HashSet();
        assertResults(rowsAsStrings, results, expected, actual);
    }

    public static void assertResultList(String[] rowsAsStrings, ResultSet results) throws SQLException {
        Collection expected = new ArrayList();
        ArrayList actual = new ArrayList();
        assertResults(rowsAsStrings, results, expected, actual);
    }
    
    private static void assertResults(String[] rowsAsStrings, ResultSet results, 
        Collection expected, Collection actual) throws SQLException {
        boolean strings = buildExpected(rowsAsStrings, expected);
        int columnsToFetch = countColumnsOfFirstRow(expected);
    
        assertEquals(expected, buildActual(results, columnsToFetch, strings, actual));
    }

    private static int countColumnsOfFirstRow(Collection expected) {
        Iterator iterator = expected.iterator();
        if (iterator.hasNext()) {
            return ((List) iterator.next()).size();
        } else {
            // We don't expect to fetch any rows in this case.
            // So if the actual has a row, getting zero columns is fine.
            return 0;
        }
    }

    private static Collection buildActual(ResultSet results, int columnsToFetch, boolean strings, Collection actual) 
    throws SQLException {
        while (results.next()) {
            L row = new L();
    
            for (int column = 1; column <= columnsToFetch; ++column) {
                Object value;
                if (strings) {
                    value = results.getString(column);
                } else {
                    value = new Integer(results.getInt(column));
                }

                if (results.wasNull()) {
                    row.append(null);
                } else {
                    row.append(value);
                }
            }
    
            actual.add(row);
        }
        results.close();
        return actual;
    }

    private static boolean buildExpected(String[] rowsAsStrings, Collection expected) {
        boolean strings = false;
        for (int i = 0; i < rowsAsStrings.length; i++) {
            String rowString = rowsAsStrings[i];
            String[] cells = rowString.split(",");
            L row = new L();
    
            for (int j = 0; j < cells.length; j++) {
                String cell = cells[j].trim();
                if (cell.startsWith("'")) {
                    strings = true;
                    row.append(cell.substring(1, cell.length() - 1));
                } else if (cell.equals("null")) {
                    row.append(null);
                } else {
                    strings = false;
                    row.append(new Integer(Integer.parseInt(cell)));
                }
            }
            expected.add(row);
        }
        return strings;
    }

    
    protected void expectQueryFailure(String sql, String expectedMessage) {
        try {
            query(sql);
            fail("Did not find expected exception.\n" +
                "expected message: " + expectedMessage + "\n" +
                "command: " + sql + "\n"
            );
        } catch (SQLException e) {
            assertMessage(expectedMessage, e);
        }
    }

    protected void expectExecuteFailure(String sql, String expectedMessage) {
        try {
            execute(sql);
            fail("Did not find expected exception.\n" +
                "expected message: " + expectedMessage + "\n" +
                "command: " + sql + "\n"
            );
        } catch (SQLException expected) {
            assertMessage(expectedMessage, expected);
        }
    }
    
}
