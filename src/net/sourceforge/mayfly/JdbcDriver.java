package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.DataStore;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

/**
 * JDBC Driver for Mayfly.
 * 
 * <p>In many cases, it will be more convenient to instantiate a
 * {@link net.sourceforge.mayfly.Database} object, and then
 * call {@link net.sourceforge.mayfly.Database#openConnection()}
 * on it (from there on out, you don't need to do anything
 * mayfly-specific).</p>
 * 
 * <p>However, if you want to create a JDBC connection via
 * non-mayfly-specific means (for example, via a database
 * mapping layer like Hibernate or SqlMaps), you may need
 * to access mayfly via a JDBC URL.  This creates a bit of
 * work in terms of getting your tests to run independently
 * of each other (one line summary: call {@link #shutdown}
 * from your tearDown method).</p>
 * 
 * <p>One connects via standard JDBC mechanisms, using
 * a JDBC URL of one of the following two forms:</p>
 * 
 * <ol>
 * <li>The URL <tt>jdbc:mayfly:</tt> means to open
 * the default database.  The default database will be
 * created if it doesn't exist (without tables or data).
 * The default database is destroyed by calling {@link #shutdown}.
 * </li>
 * 
 * <li>If you want to start a database with some tables or
 * data, you can call {@link #create(DataStore)}, which will
 * return a new URL to you.  This database lives until the next
 * call to {@link #shutdown}.
 * </li>
 * </ol>
 * 
 * <p>If you specify a username or a password via JDBC,
 * Mayfly ignores them.</p>
 * 
 * <p>Example:</p>
 * <pre>
 * Class.forName("net.sourceforge.mayfly.JdbcDriver");
 * Connection connection = DriverManager.getConnection("jdbc:mayfly:");
 * </pre>
 */
public class JdbcDriver implements Driver {

    /** 
     * <p>Create a database which you want to access via a JDBC URL.
     * For many purposes, it will be more convenient to
     * instantiate a {@link Database} object, but if you need a
     * JDBC URL (for example, to pass to a database mapping layer
     * like Hibernate or SqlMaps), call this method instead.</p>
     * <p>Example:</p>
    <pre>
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

    String jdbcUrl;
    public void setUp() {
    &nbsp;&nbsp;&nbsp;&nbsp;jdbcUrl = JdbcDriver.create(standardSetup);
    }
    
    public void tearDown() {
    &nbsp;&nbsp;&nbsp;&nbsp;JdbcDriver.shutdown();
    }
    </pre>
     * @param dataStore The initial contents of the database.
     */
    public static String create(DataStore dataStore) {
        return getMayflyDriver().createInDriver(dataStore);
    }

    /**
     * <p>Destroy databases managed by {@link JdbcDriver}.  That is,
     * all databases which have been created with {@link #create(DataStore)},
     * plus the default database (the one with url <tt>jdbc:mayfly:</tt>).</p>
     * 
     * <p>Databases created by calling constructors of {@link Database} directly
     * are instead garbage collected like any other object.</p>
     */
    public static void shutdown() {
        getMayflyDriver().shutdownInDriver();
    }

    private static JdbcDriver getMayflyDriver() {
        try {
            return (JdbcDriver) DriverManager.getDriver(JDBC_URL_PREFIX);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String JDBC_URL_PREFIX = "jdbc:mayfly:";
    private static final String DEFAULT_DATABASE = JDBC_URL_PREFIX;

    static {
        try {
            DriverManager.registerDriver(new JdbcDriver());
        } catch (SQLException e) {
            // This prevents the class from being loaded and propagates
            // some kind of exception up to whoever tried to load it, right?
            // Is that right?  Is there another way to handle it?
            throw new RuntimeException(e);
        }
    }
    
    HashMap databases = new HashMap();
    int nextId;

    public Connection connect(String url, Properties info) throws SQLException {
        return findDatabase(url).openConnection();
    }

    private Database findDatabase(String url) throws SQLException {
        if (DEFAULT_DATABASE.equals(url)) {
            if (!databases.containsKey(DEFAULT_DATABASE)) {
                databases.put(DEFAULT_DATABASE, new Database());
            }
        }
        
        if (databases.containsKey(url)) {
            return (Database) databases.get(url);
        } else {
            throw new SQLException("Mayfly JDBC URL " + url + " not recognized");
        }
    }

    private String createInDriver(DataStore dataStore) {
        String url = JDBC_URL_PREFIX + nextId++;
        databases.put(url, new Database(dataStore));
        return url;
    }

    private void shutdownInDriver() {
        databases = new HashMap();
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.startsWith(JDBC_URL_PREFIX);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) throws SQLException {
        throw new UnimplementedException();
    }

    public int getMajorVersion() {
        throw new UnimplementedException();
    }

    public int getMinorVersion() {
        throw new UnimplementedException();
    }

    public boolean jdbcCompliant() {
        return false;
    }

}
