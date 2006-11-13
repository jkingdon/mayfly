package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.jdbc.JdbcConnection;
import net.sourceforge.mayfly.parser.Parser;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A Database contains a set of tables, but can be managed as easily as
 * any other object.
 * 
 * <p>Here's an example of what your test might look like:</p>
 * <pre>
 * private Database database;
 * public void setUp() throws Exception {
 * &nbsp;&nbsp;&nbsp;&nbsp;database = new Database();
 * }
 * 
 * public void tearDown() throws Exception {
 * &nbsp;&nbsp;&nbsp;&nbsp;// Nulling variables can hurt performance in general, but 
 * &nbsp;&nbsp;&nbsp;&nbsp;// might be helpful here because JUnit3 keeps test case
 * &nbsp;&nbsp;&nbsp;&nbsp;// objects around until the end of a test run.
 * &nbsp;&nbsp;&nbsp;&nbsp;database = null;
 * }
 * 
 * public void testData() throws Exception {
 * &nbsp;&nbsp;&nbsp;&nbsp;connection = database.openConnection();
 * &nbsp;&nbsp;&nbsp;&nbsp;. . .
 * &nbsp;&nbsp;&nbsp;&nbsp;connection.close();
 * }
 * </pre>
 * 
 * <p>In the above example, we first call {@link #openConnection()}
 * and then perform all operations via that connection.  There are
 * also a variety of methods to operate on the database directly
 * (for example, {@link #execute(String)}.
 * These are basically convenience methods; most/all of these operations
 * can also be done via connections.  These methods take per-connection settings 
 * (for example the current schema as set by
 * SET SCHEMA, or the auto-commit flag) from a <i>default connection</i>;
 * that is they are shared with other calls which use the default connection,
 * but not with connections opened explicitly (for example
 * via {@link #openConnection()}).
 */
public class Database {

    private DataStore dataStore;
    private final MayflyConnection defaultConnection;

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
        setDataStore(store);
        defaultConnection = new MayflyConnection(this);
    }

    /**
     * Execute an SQL command which does not return results.
     * This is similar to the JDBC java.sql.Statement#executeUpdate(java.lang.String)
     * but is more convenient if you have a Database instance around.
     * 
     * @return Number of rows changed.
     */
    public int execute(String sql) throws MayflyException {
        return defaultConnection.execute(sql);
    }

    /**
     * Execute a series of SQL commands separated by semicolons.
     * This method closes the reader when done.
     */
    public void executeScript(Reader script) throws MayflyException {
        try {
            List commands = new Parser(script).parseCommands();
            for (Iterator iter = commands.iterator(); iter.hasNext();) {
                Command command = (Command) iter.next();
                defaultConnection.executeUpdate(command);
            }
        }
        finally {
            try {
                script.close();
            } catch (IOException e) {
                // Location should probably be where we stopped
                // reading, which I guess is always end of file.
                throw new MayflyException(e);
            }
        }
    }

    /**
     * @internal
     * Only intended for use within Mayfly.
     */
    public UpdateStore executeUpdate(Command command, String currentSchema) {
        UpdateStore result = command.update(dataStore, currentSchema);
        dataStore = result.store();
        return result;
    }

    /**
     * Execute an SQL command which returns results.
     * 
     * This is similar to the JDBC java.sql.Statement#executeQuery(java.lang.String)
     * but is more convenient if you have a Database instance around.
     */
    public ResultSet query(String sql) throws MayflyException {
        return defaultConnection.query(sql);
    }
    
    /**
     * List your tables, as names.
     * 
     * <p>The returned list only includes tables
     * which you have explicitly created; there are no tables here
     * which are for Mayfly's own use.  If you have more than one
     * schema, only tables in the current
     * schema (as set by SET SCHEMA) are returned.</p>
     * 
     * <p>If a future version of Mayfly implements this functionality in
     * java.sql.DatabaseMetaData, this method is likely to remain, as
     * being more convenient than DatabaseMetaData.</p>
     */
    public Set tables() {
        return defaultConnection.tables();
    }

    /**
     * Return the schema names for this Database.
     * 
     * <p>This returned list only includes schemas
     * which you have explicitly created.  The anonymous schema is not
     * included in the returned list, but will always exist.</p>
     * 
     * <p>If a future version of Mayfly implements this functionality in
     * java.sql.DatabaseMetaData, this method is likely to remain, as
     * being more convenient than DatabaseMetaData.</p>
     */
    public Set schemas() {
        return dataStore.schemas();
    }

    /**
     * Return the column names in the given table.
     * 
     * <p>If a future version of Mayfly implements this functionality in
     * java.sql.DatabaseMetaData, this method may go away or become
     * some kind of convenience method.</p>
     */
    public List columnNames(String tableName) {
        return defaultConnection.columnNames(tableName);
    }

    /**
     * Number of rows in given table.
     * 
     * This is a convenience method.  Your production code will almost
     * surely be counting rows (if it needs to at all) via
     * java.sql.ResultSet (or the SQL COUNT expression).
     * But this method may be convenient in tests.
     */
    public int rowCount(String tableName) {
        return defaultConnection.rowCount(tableName);
    }

    /**
     * Open a JDBC connection.
     * 
     * This is similar to the JDBC java.sql.DriverManager#getConnection(java.lang.String)
     * but is based on this Database, rather than the static Database used in the JDBC case.
     */
    public Connection openConnection() {
        return new JdbcConnection(this);
    }

    /**
     * Take a snapshot of this database.
     * 
     * Specifically, return the data store, which is
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

    /**
     * @internal
     * Only intended for use within Mayfly.
     * The idea is that public callers call
     * {@link #Database(DataStore)}.
     * 
     * The idea of setting the datastore is a
     * dangerous one - what if someone else has changed
     * the database in the meantime?  Are we supposed
     * to detect a conflict?  Or merge the changes?
     * 
     * So, yes, {@link #Database(DataStore)} is a
     * more sensible interface.
     */
    public void setDataStore(DataStore store) {
        if (store == null) {
            throw new NullPointerException("Attempt to set data store to null");
        }
        dataStore = store;
    }

}
