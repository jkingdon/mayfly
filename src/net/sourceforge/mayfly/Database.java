package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.jdbc.*;
import net.sourceforge.mayfly.ldbc.*;

import java.sql.*;
import java.util.*;

/**
 * A Database contains a set of tables, but can be managed as easily as
 * any other object.
 * 
 * <p>Example:</p>
 * <pre>
 * private Database database;
 * public void setUp() throws Exception {
 * &nbsp;&nbsp;&nbsp;&nbsp;database = new Database();
 * }
 * 
 * public void tearDown() throws Exception {
 * &nbsp;&nbsp;&nbsp;&nbsp;// Nulling variables can hurt performance in general, but 
 * &nbsp;&nbsp;&nbsp;&nbsp;// might be helpful here because JUnit keeps objects around
 * &nbsp;&nbsp;&nbsp;&nbsp;// until the end of a test run.
 * &nbsp;&nbsp;&nbsp;&nbsp;database = null;
 * }
 * 
 * public void testData() throws Exception {
 * &nbsp;&nbsp;&nbsp;&nbsp;connection = database.openConnection();
 * &nbsp;&nbsp;&nbsp;&nbsp;. . .
 * &nbsp;&nbsp;&nbsp;&nbsp;connection.close();
 * }
 * </pre>
 */
public class Database {

    private DataStore dataStore;
    private String currentSchema;

    /**
     * Create an empty database (one with no tables).
     */
    public Database() {
        this(new DataStore());
    }

    /**
     * Create a database starting with the contents of a {@link net.sourceforge.mayfly.datastore.DataStore},
     * which you'd normally get from the {@link #dataStore()} method of
     * another {@link Database} object.
     */
    public Database(DataStore store) {
        dataStore = store;
        currentSchema = DataStore.ANONYMOUS_SCHEMA_NAME;
    }

    /**
     * Execute an SQL command which does not return results.
     * This is similar to the JDBC java.sql.Statement#executeUpdate(java.lang.String)
     * but is more convenient if you have a Database instance around.
     * @return Number of rows changed.
     */
    public int execute(String sql) throws SQLException {
        try {
            Command command = Command.fromTree(Tree.parse(sql));
            return executeUpdate(command);
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    /**
     * @internal
     * Only intended for use within Mayfly.
     */
    public int executeUpdate(Command command) {
        if (command instanceof SetSchema) {
            SetSchema setSchema = (SetSchema) command;
            String proposed = setSchema.name();
            dataStore.schema(proposed);
            currentSchema = proposed;
            return setSchema.rowsAffected();
        }
        dataStore = command.update(dataStore, currentSchema);
        return command.rowsAffected();
    }

    /**
     * Execute an SQL command which returns results.
     * This is similar to the JDBC java.sql.Statement#executeQuery(java.lang.String)
     * but is more convenient if you have a Database instance around.
     */
    public ResultSet query(String command) throws SQLException {
        try {
            Select select = Select.selectFromTree(Tree.parse(command));
            return select.select(dataStore, currentSchema);
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }
    
    /**
     * @internal
     * Only intended for use within Mayfly.
     */
    public ResultSet query(Select select) {
        return select.select(dataStore, currentSchema);
    }

    /**
     * <p>Return table names.  The returned list only includes tables
     * which you have explicitly created; there are no tables here
     * which are for Mayfly's own use.</p>
     * 
     * <p>If a future version of Mayfly implements this functionality in
     * java.sql.DatabaseMetaData, this method may go away or become
     * a convenience method.</p>
     */
    public Set tables() {
        return dataStore.tables(currentSchema);
    }

    /**
     * <p>Return schema names.  This returned list only includes schemas
     * which you have explicitly created.  The anonymous schema is not
     * included in the returned list, but will always exist.</p>
     * 
     * <p>If a future version of Mayfly implements this functionality in
     * java.sql.DatabaseMetaData, this method may go away or become
     * a convenience method.</p>
     */
    public Set schemas() {
        return dataStore.schemas();
    }

    /**
     * <p>Column names in given table.</p>
     * 
     * <p>If a future version of Mayfly implements this functionality in
     * java.sql.DatabaseMetaData, this method may go away or become
     * some kind of convenience method.</p>
     */
    public List columnNames(String tableName) throws SQLException {
        TableData tableData = dataStore.table(currentSchema, tableName);
        return tableData.columnNames();
    }

    /**
     * Number of rows in given table.
     * 
     * This is a convenience method.  Your production code will almost
     * surely be counting rows (if it needs to at all) via
     * java.sql.ResultSet (or the SQL COUNT, once Mayfly implements it).
     * But this method may be convenient in tests.
     */
    public int rowCount(String tableName) throws SQLException {
        TableData tableData = dataStore.table(currentSchema, tableName);
        return tableData.rowCount();
    }

    /**
     * Open a JDBC connection.
     * This is similar to the JDBC java.sql.DriverManager#getConnection(java.lang.String)
     * but is more convenient if you have a Database instance around.
     */
    public Connection openConnection() throws SQLException {
        return new JdbcConnection(this);
    }

    /**
     * Take a snapshot of this database.  Specifically, return the data store, which is
     * an immutable object containing all the data, and table definitions, for this
     * database.  Because the data store is immutable, one might store it in a constant
     * and use it from multiple tests.  Here's an example:
     * 
     * <pre>
    static final DataStore standardSetup = makeData();

    private static DataStore makeData() {
    &nbsp;&nbsp;&nbsp;&nbsp;try {
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Database original = new Database();
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;original.execute("create table foo (a integer)");
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;original.execute("insert into foo(a) values(6)");
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return original.dataStore();
    &nbsp;&nbsp;&nbsp;&nbsp;} catch (SQLException e) {
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;throw new RuntimeException(e);
    &nbsp;&nbsp;&nbsp;&nbsp;}
    }

    Database database;
    public void setUp() {
    &nbsp;&nbsp;&nbsp;&nbsp;database = new Database(standardSetup);
    }
    </pre>
    
    @see {@link JdbcDriver#create(DataStore)}
     */
    public DataStore dataStore() {
        return dataStore;
    }

}
