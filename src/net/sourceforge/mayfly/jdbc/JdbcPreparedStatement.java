package net.sourceforge.mayfly.jdbc;

import net.sourceforge.mayfly.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class JdbcPreparedStatement implements PreparedStatement {

    private final String command;

    public JdbcPreparedStatement(String sql) {
        this.command = sql;
    }

    public ResultSet executeQuery() throws SQLException {
        if (command == null) {
            throw new NullPointerException();
        }
        throw new UnimplementedException();
    }

    public int executeUpdate() throws SQLException {
        throw new UnimplementedException();
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throw new UnimplementedException();
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void clearParameters() throws SQLException {
        throw new UnimplementedException();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType,
            int scale) throws SQLException {
        throw new UnimplementedException();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean execute() throws SQLException {
        throw new UnimplementedException();
    }

    public void addBatch() throws SQLException {
        throw new UnimplementedException();
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setBlob(int i, Blob x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setClob(int i, Clob x) throws SQLException {
        throw new UnimplementedException();
    }

    public void setArray(int i, Array x) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnimplementedException();
    }

    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setNull(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new UnimplementedException();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        throw new UnimplementedException();
    }

    public int executeUpdate(String sql) throws SQLException {
        throw new UnimplementedException();
    }

    public void close() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxFieldSize() throws SQLException {
        throw new UnimplementedException();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxRows() throws SQLException {
        throw new UnimplementedException();
    }

    public void setMaxRows(int max) throws SQLException {
        throw new UnimplementedException();
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnimplementedException();
    }

    public int getQueryTimeout() throws SQLException {
        throw new UnimplementedException();
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        throw new UnimplementedException();
    }

    public void cancel() throws SQLException {
        throw new UnimplementedException();
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnimplementedException();
    }

    public void clearWarnings() throws SQLException {
        throw new UnimplementedException();
    }

    public void setCursorName(String name) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean execute(String sql) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getResultSet() throws SQLException {
        throw new UnimplementedException();
    }

    public int getUpdateCount() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean getMoreResults() throws SQLException {
        throw new UnimplementedException();
    }

    public void setFetchDirection(int direction) throws SQLException {
        throw new UnimplementedException();
    }

    public int getFetchDirection() throws SQLException {
        throw new UnimplementedException();
    }

    public void setFetchSize(int rows) throws SQLException {
        throw new UnimplementedException();
    }

    public int getFetchSize() throws SQLException {
        throw new UnimplementedException();
    }

    public int getResultSetConcurrency() throws SQLException {
        throw new UnimplementedException();
    }

    public int getResultSetType() throws SQLException {
        throw new UnimplementedException();
    }

    public void addBatch(String sql) throws SQLException {
        throw new UnimplementedException();
    }

    public void clearBatch() throws SQLException {
        throw new UnimplementedException();
    }

    public int[] executeBatch() throws SQLException {
        throw new UnimplementedException();
    }

    public Connection getConnection() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean getMoreResults(int current) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        throw new UnimplementedException();
    }

    public int executeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException {
        throw new UnimplementedException();
    }

    public int executeUpdate(String sql, int[] columnIndexes)
            throws SQLException {
        throw new UnimplementedException();
    }

    public int executeUpdate(String sql, String[] columnNames)
            throws SQLException {
        throw new UnimplementedException();
    }

    public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException {
        throw new UnimplementedException();
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean execute(String sql, String[] columnNames)
            throws SQLException {
        throw new UnimplementedException();
    }

    public int getResultSetHoldability() throws SQLException {
        throw new UnimplementedException();
    }

}
