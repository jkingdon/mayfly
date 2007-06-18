package net.sourceforge.mayfly.jdbc;

import net.sourceforge.mayfly.MayflyConnection;
import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.TimestampCell;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.parser.Lexer;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.parser.Substitutor;
import net.sourceforge.mayfly.util.ImmutableByteArray;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

public class JdbcPreparedStatement implements PreparedStatement {

    private final MayflyConnection mayflyConnection;

    private final String sql;
    private Vector parameters;
    private final int parameterCount;
    private BitSet parameterSpecified;

    JdbcPreparedStatement(String sql, MayflyConnection mayflyConnection) 
    throws SQLException {
        this.mayflyConnection = mayflyConnection;

        try {
            this.sql = sql;
            this.parameters = new Vector();

            List tokens = new Lexer(sql).tokens();
            parameterCount = Substitutor.parameterCount(tokens);
            new Parser(tokens, true).parse();

            parameters.setSize(parameterCount);
            parameterSpecified = new BitSet(parameterCount);
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    public ResultSet executeQuery() throws SQLException {
        try {
            Command select = fromTokens();
            return mayflyConnection.query(select);
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    public int executeUpdate() throws SQLException {
        try {
            Command command = fromTokens();
            return mayflyConnection.executeUpdate(command);
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    private Command fromTokens() throws SQLException {
        return Command.fromTokens(substitutedTokens(), mayflyConnection.options());
    }

    private List substitutedTokens() throws SQLException {
        // This didn't work for me.  Perhaps a bug in which libgcj
        // was returning an answer past the length of the bitset, but
        // I didn't investigate enough to be sure.
//        int firstUnspecifiedParameter = parameterSpecified.nextClearBit(0);
//        if (firstUnspecifiedParameter != -1) {
//            int oneBased = firstUnspecifiedParameter + 1;
//            throw new MayflyException("Parameter " + oneBased + " missing");
//        }
        for (int i = 0; i < parameterCount; ++i) {
            if (!parameterSpecified.get(i)) {
                int oneBased = i + 1;
                throw new MayflyException("Parameter " + oneBased + " missing");
            }
        }
        
        return Substitutor.substitute(new Lexer(sql).tokens(), parameters);
    }

    public void setNull(int oneBased, int sqlType) throws SQLException {
        setParameter(oneBased, null);
    }

    public void setBoolean(int parameterIndex, boolean value) throws SQLException {
        throw new UnimplementedException();
    }

    public void setByte(int oneBased, byte value) throws SQLException {
        setParameter(oneBased, new Long(value));
    }

    public void setShort(int oneBased, short value) throws SQLException {
        setParameter(oneBased, new Long(value));
    }

    public void setInt(int oneBased, int value) throws SQLException {
        setParameter(oneBased, new Long(value));
    }

    public void setLong(int oneBased, long value) throws SQLException {
        setParameter(oneBased, new Long(value));
    }

    private void setParameter(int oneBased, Object value) throws SQLException {
        int zeroBased = oneBased - 1;

        if (zeroBased < 0) {
            throw new SQLException("Parameter index " + oneBased + " is out of bounds");
        }

        if (zeroBased >= parameterCount) {
            throw new SQLException("Parameter index " + oneBased + " is out of bounds");
        }

        parameters.set(zeroBased, value);
        parameterSpecified.set(zeroBased);
    }

    public void setFloat(int parameterIndex, float value) throws SQLException {
        throw new UnimplementedException();
    }

    public void setDouble(int parameterIndex, double value) throws SQLException {
        /* What's the difference between the BigDecimal constructor that
           takes a double and the one that takes a string? */
        setParameter(parameterIndex, new BigDecimal(String.valueOf(value)));
    }

    public void setBigDecimal(int parameterIndex, BigDecimal value)
            throws SQLException {
        setParameter(parameterIndex, value);
    }

    public void setString(int parameterIndex, String value) throws SQLException {
        setParameter(parameterIndex, value);
    }

    public void setBytes(int parameterIndex, byte[] value) throws SQLException {
        setParameter(parameterIndex, new ImmutableByteArray(value));
    }

    public void setDate(int parameterIndex, java.sql.Date value) 
    throws SQLException {
        long time = value.getTime();
        String string = new LocalDate(time).toString();
        setParameter(parameterIndex, string);
    }

    public void setTime(int parameterIndex, Time value) throws SQLException {
        throw new UnimplementedException();
    }

    public void setTimestamp(int parameterIndex, Timestamp value)
            throws SQLException {
        long time = value.getTime();
        String string = TimestampCell.FORMATTER.print(time);
        setParameter(parameterIndex, string);
    }

    public void setAsciiStream(int parameterIndex, InputStream value, int length)
            throws SQLException {
        throw new UnimplementedException("We recommend setCharacterStream instead");
    }

    /**
     * @deprecated
     */
    public void setUnicodeStream(int parameterIndex, InputStream value, int length)
            throws SQLException {
        throw new UnimplementedException("We recommend setCharacterStream instead");
    }

    public void setBinaryStream(int parameterIndex, InputStream stream, int length)
            throws SQLException {
        try {
            try {
                setParameter(parameterIndex, new ImmutableByteArray(stream));
            }
            finally {
                stream.close();
            }
        }
        catch (IOException e) {
            throw (SQLException) new SQLException("Cannot read parameter")
                .initCause(e);
        }
    }

    public void clearParameters() throws SQLException {
        throw new UnimplementedException();
    }

    public void setObject(int parameterIndex, Object value, int targetSqlType,
            int scale) throws SQLException {
        throw new UnimplementedException();
    }

    public void setObject(int parameterIndex, Object value, int targetSqlType)
            throws SQLException {
        setParameter(parameterIndex, value);
    }

    public void setObject(int parameterIndex, Object value) throws SQLException {
        setParameter(parameterIndex, value);
    }

    public boolean execute() throws SQLException {
        throw new UnimplementedException();
    }

    public void addBatch() throws SQLException {
        throw new UnimplementedException();
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws SQLException {
        try {
            setString(parameterIndex, readString(reader));
        } catch (IOException e) {
            throw (SQLException) new SQLException("Cannot read parameter")
                .initCause(e);
        }
    }
    
    private String readString(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        char[] buffer = new char[8192];
        int n = 0;
        while (-1 != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
        }
        return writer.toString();
    }

    public void setRef(int i, Ref value) throws SQLException {
        throw new UnimplementedException();
    }

    public void setBlob(int i, Blob value) throws SQLException {
        throw new UnimplementedException();
    }

    public void setClob(int i, Clob value) throws SQLException {
        throw new UnimplementedException();
    }

    public void setArray(int i, Array value) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnimplementedException();
    }

    public void setDate(int parameterIndex, java.sql.Date value, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setTime(int parameterIndex, Time value, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setTimestamp(int parameterIndex, Timestamp value, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public void setNull(int oneBased, int sqlType, String typeName)
            throws SQLException {
        setNull(oneBased, sqlType);
    }

    public void setURL(int parameterIndex, URL value) throws SQLException {
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
    }

    public int getMaxFieldSize() throws SQLException {
        throw new UnimplementedException();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxRows() throws SQLException {
        return 0;
    }

    public void setMaxRows(int max) throws SQLException {
        throw new UnimplementedException();
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnimplementedException();
    }

    public int getQueryTimeout() throws SQLException {
        return 0;
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
