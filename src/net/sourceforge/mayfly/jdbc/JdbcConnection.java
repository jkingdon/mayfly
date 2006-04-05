package net.sourceforge.mayfly.jdbc;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.MayflyConnection;
import net.sourceforge.mayfly.UnimplementedException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

public class JdbcConnection implements Connection {

    private MayflyConnection mayflyConnection;

    /**
     * Should only be called directly from within Mayfly.  External callers should call
     * {@link Database.openConnection()} or {@link DriverManager.getConnection(String)}.
     */
    public JdbcConnection(Database database) {
        this.mayflyConnection = new MayflyConnection(database);
    }

    public Statement createStatement() throws SQLException {
        return new JdbcStatement(mayflyConnection);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new JdbcPreparedStatement(sql, mayflyConnection);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new UnimplementedException();
    }

    public String nativeSQL(String sql) throws SQLException {
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

    public void close() throws SQLException {
    }

    public boolean isClosed() throws SQLException {
        throw new UnimplementedException();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        throw new UnimplementedException();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isReadOnly() throws SQLException {
        throw new UnimplementedException();
    }

    public void setCatalog(String catalog) throws SQLException {
        throw new UnimplementedException();
    }

    public String getCatalog() throws SQLException {
        throw new UnimplementedException();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        // Not sure what we should do with this.
    }

    public int getTransactionIsolation() throws SQLException {
        throw new UnimplementedException();
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnimplementedException();
    }

    public void clearWarnings() throws SQLException {
        throw new UnimplementedException();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        throw new UnimplementedException();
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        throw new UnimplementedException();
    }

    public Map getTypeMap() throws SQLException {
        throw new UnimplementedException();
    }

    public void setTypeMap(Map map) throws SQLException {
        throw new UnimplementedException();
    }

    public void setHoldability(int holdability) throws SQLException {
        throw new UnimplementedException();
    }

    public int getHoldability() throws SQLException {
        throw new UnimplementedException();
    }

    public Savepoint setSavepoint() throws SQLException {
        throw new UnimplementedException();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        throw new UnimplementedException();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        throw new UnimplementedException();
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new UnimplementedException();
    }

    public Statement createStatement(int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        throw new UnimplementedException();
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws SQLException {
        throw new UnimplementedException();
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException {
        throw new UnimplementedException();
    }

}
