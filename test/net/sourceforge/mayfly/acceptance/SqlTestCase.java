package net.sourceforge.mayfly.acceptance;

import junit.framework.TestCase;

import net.sourceforge.mayfly.util.L;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class SqlTestCase extends TestCase {

    protected Dialect dialect =
        //new MayflyDialect()
        new HypersonicDialect()
        //new MySqlDialect()
        //new PostgresDialect()
        //new DerbyDialect()

        //new SmallSqlDialect()
        //new MySql4Dialect()
    ;

    // Turn this on to see a comparison of mayfly exception messages with
    // the current database's messages.
    static final boolean SHOW_MESSAGES = false;

    /**
     * @internal
     * Like {@link #SHOW_MESSAGES} but the whole stack trace (in case a database
     * includes vital information other than in the message, which doesn't
     * usually seem to be the case).
     * Enable this or {@link #SHOW_MESSAGES} but not both.
     */
    static final boolean SHOW_STACK_TRACES = false;

    protected Connection connection;
    private Statement statement;

    public void setUp() throws Exception {
        connection = dialect.openConnection();
    }

    public void tearDown() throws Exception {
        dialect.shutdown(connection);

        if (statement != null) {
            statement.close();
        }
        connection.close();
    }
    
    protected int execute(String sql) throws SQLException {
        return execute(sql, connection);
    }

    static int execute(String sql, Connection connection) throws SQLException {
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
        dialect.assertTableCount(expected);
    }

    protected void assertMessage(String expectedMessage, SQLException exception) {
        dialect.assertMessage(expectedMessage, exception);
    }

    /**
     * Like {@link #assertResultList(String[], ResultSet)} but using a set instead
     * of a list.  This has two big consequences: (1) order is not important
     * (this matters when testing ORDER BY and similar features), and (2) duplicates
     * are removed (this matters when testing DISTINCT and similar features).
     */
    public static void assertResultSet(String[] rowsAsStrings, ResultSet results) 
    throws SQLException {
        Collection expected = new HashSet();
        HashSet actual = new HashSet();
        assertResults(rowsAsStrings, results, expected, actual);
    }

    public static void assertResultList(String[] rowsAsStrings, ResultSet results) 
    throws SQLException {
        Collection expected = new ArrayList();
        ArrayList actual = new ArrayList();
        assertResults(rowsAsStrings, results, expected, actual);
    }
    
    private static void assertResults(String[] rowsAsStrings, ResultSet results, 
        Collection expected, Collection actual) throws SQLException {
        BitSet strings = buildExpected(rowsAsStrings, expected);
        int columnsToFetch = countColumnsOfFirstRow(expected);
        
        buildActual(results, columnsToFetch, strings, actual);
        
        assertEquals(expected, actual);
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

    private static void buildActual(ResultSet results, int columnsToFetch, BitSet strings, Collection actual) 
    throws SQLException {
        while (results.next()) {
            L row = new L();
    
            for (int column = 1; column <= columnsToFetch; ++column) {
                Object value;
                if (strings.get(column - 1)) {
                    value = results.getString(column);
                } else {
                    value = new Long(results.getLong(column));
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
    }

    private static BitSet buildExpected(String[] rowsAsStrings, Collection expected) {
        BitSet strings = null;
        for (int i = 0; i < rowsAsStrings.length; i++) {
            String rowString = rowsAsStrings[i];
            String[] cells = rowString.split(",");
            L row = new L();
            
            if (strings == null) {
                strings = new BitSet(cells.length);
            }
    
            for (int j = 0; j < cells.length; j++) {
                String cell = cells[j].trim();
                if (cell.startsWith("'")) {
                    strings.set(j);
                    row.append(cell.substring(1, cell.length() - 1));
                } else if (cell.equals("null")) {
                    row.append(null);
                } else {
                    strings.clear(j);
                    row.append(new Long(cell));
                }
            }
            expected.add(row);
        }
        return strings;
    }

    
    protected void expectQueryFailure(String sql, String expectedMessage) {
        try {
            query(sql);
            failForMissingException(sql, expectedMessage);
        } catch (SQLException e) {
            assertMessage(expectedMessage, e);
        }
    }

    protected void expectQueryFailure(String sql, String expectedMessage, 
        int expectedStartLine, int expectedStartColumn,
        int expectedEndLine, int expectedEndColumn) {
        try {
            query(sql);
            failForMissingException(sql, expectedMessage);
        } catch (SQLException expected) {
            dialect.assertMessage(expectedMessage, expected, 
                expectedStartLine, expectedStartColumn, expectedEndLine, expectedEndColumn);
        }
    }

    protected void expectExecuteFailure(String sql, String expectedMessage) {
        try {
            execute(sql);
            failForMissingException(sql, expectedMessage);
        } catch (SQLException expected) {
            assertMessage(expectedMessage, expected);
        }
    }

    protected void expectExecuteFailure(String sql, String expectedMessage, 
        int expectedStartLine, int expectedStartColumn,
        int expectedEndLine, int expectedEndColumn) {
        try {
            execute(sql);
            failForMissingException(sql, expectedMessage);
        } catch (SQLException expected) {
            dialect.assertMessage(expectedMessage, expected, 
                expectedStartLine, expectedStartColumn, expectedEndLine, expectedEndColumn);
        }
    }

    public static void failForMissingException(String sql, String expectedMessage) {
        fail("Did not find expected exception.\n" +
            "expected message: " + expectedMessage + "\n" +
            "command: " + sql + "\n"
        );
    }
    
    public void createEmptySchema(String name) throws SQLException {
        assertEquals(0, execute(dialect.createEmptySchemaCommand(name)));
        assertEquals(0, execute("set schema " + name));
    }

}
