package net.sourceforge.mayfly.jdbc;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.JdbcDriver;
import net.sourceforge.mayfly.MayflyConnection;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.DataStore;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

/**
 * This is Mayfly's implementation of a JDBC connection.
 * For the most part, standard JDBC methods should suffice,
 * but there are a few Mayfly-specific methods.  This
 * is so that you can take your connection, cast it to
 * {@link JdbcConnection}, and then call a method such
 * as {@link #snapshot()}.  An alternative is the methods
 * in {@link JdbcDriver} such as {@link JdbcDriver#snapshot(String url)};
 * it is a matter of convenience which you wish to call.
 * 
 * @internal
 * The responsibility of this class is to handle the various
 * methods of JDBC, including the uninteresting ones.
 * 
 * The responsibility of {@link MayflyConnection} is to
 * deal with the meat of a connection - auto-commit flag,
 * execution, etc.
 * 
 * At least, I (kingdon) don't remember/see any other reason for
 * separating them.
 */
public class JdbcConnection implements Connection {

    private MayflyConnection mayflyConnection;

    /**
     * Should only be called directly from within Mayfly.  External callers should call
     * {@link Database.openConnection()} or DriverManager.getConnection(String).
     */
    public JdbcConnection(Database database) {
        this.mayflyConnection = new MayflyConnection(database);
    }

    public Statement createStatement() throws SQLException {
        checkClosed();
        return new JdbcStatement(mayflyConnection);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkClosed();
        return new JdbcPreparedStatement(sql, mayflyConnection);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public String nativeSQL(String sql) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        mayflyConnection.setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        return mayflyConnection.getAutoCommit();
    }

    public void commit() throws SQLException {
        mayflyConnection.commit();
    }

    public void rollback() throws SQLException {
        mayflyConnection.rollback();
    }

    /**
     * @internal
     * In some cases this will make the {@link Database}
     * available for garbage collection.
     */
    public void close() throws SQLException {
        mayflyConnection = null;
    }

    public boolean isClosed() throws SQLException {
        return mayflyConnection == null;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        checkClosed();
        return new JdbcMetaData(mayflyConnection);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public boolean isReadOnly() throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public void setCatalog(String catalog) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public String getCatalog() throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        checkClosed();
        // Not sure what we should do with this.
    }

    public int getTransactionIsolation() throws SQLException {
        checkClosed();
        return TRANSACTION_NONE;
    }

    public SQLWarning getWarnings() throws SQLException {
        checkClosed();
        return null;
    }

    public void clearWarnings() throws SQLException {
        checkClosed();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public Map getTypeMap() throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public void setTypeMap(Map map) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public void setHoldability(int holdability) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public int getHoldability() throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public Savepoint setSavepoint() throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public Statement createStatement(int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException {
        checkClosed();
        throw new UnimplementedException();
    }

    private void checkClosed() throws SQLException {
        if (mayflyConnection == null) {
            throw new SQLException("connection is closed");
        }
    }

    /**
     * Take a snapshot of this database.
     * 
     * Specifically, return the data store, which is
     * an immutable object containing all the data, and table definitions, for this
     * database.
     */
    public DataStore snapshot() {
        return mayflyConnection.snapshot();
    }

}
