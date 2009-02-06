package net.sourceforge.mayfly;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

class ResultSetStub implements ResultSet {

    public boolean next() throws SQLException {
        throw new UnimplementedException();
    }

    public void close() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean wasNull() throws SQLException {
        throw new UnimplementedException();
    }

    public String getString(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public byte getByte(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public short getShort(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public int getInt(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public long getLong(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public float getFloat(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public double getDouble(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    /**
     * @deprecated
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws SQLException {
        throw new UnimplementedException();
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public Date getDate(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public Time getTime(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    /**
     * @deprecated
     */
    public InputStream getUnicodeStream(int columnIndex)
            throws SQLException {
        throw new UnimplementedException();
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public String getString(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean getBoolean(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public byte getByte(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public short getShort(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public int getInt(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public long getLong(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public float getFloat(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public double getDouble(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    /**
     * @deprecated
     */
    public BigDecimal getBigDecimal(String columnName, int scale)
            throws SQLException {
        throw new UnimplementedException();
    }

    public byte[] getBytes(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public Date getDate(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public Time getTime(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public InputStream getAsciiStream(String columnName)
            throws SQLException {
        throw new UnimplementedException();
    }

    /**
     * @deprecated
     */
    public InputStream getUnicodeStream(String columnName)
            throws SQLException {
        throw new UnimplementedException();
    }

    public InputStream getBinaryStream(String columnName)
            throws SQLException {
        throw new UnimplementedException();
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnimplementedException();
    }

    public void clearWarnings() throws SQLException {
        throw new UnimplementedException();

    }

    public String getCursorName() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnimplementedException();
    }

    public Object getObject(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public Object getObject(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public int findColumn(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isBeforeFirst() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isAfterLast() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isFirst() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isLast() throws SQLException {
        throw new UnimplementedException();
    }

    public void beforeFirst() throws SQLException {
        throw new UnimplementedException();

    }

    public void afterLast() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean first() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean last() throws SQLException {
        throw new UnimplementedException();
    }

    public int getRow() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean absolute(int row) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean relative(int rows) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean previous() throws SQLException {
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

    public int getType() throws SQLException {
        throw new UnimplementedException();
    }

    public int getConcurrency() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean rowUpdated() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean rowInserted() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean rowDeleted() throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNull(int columnIndex) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBoolean(int columnIndex, boolean x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateString(int columnIndex, String x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateTimestamp(int columnIndex, Timestamp x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBinaryStream(int columnIndex, InputStream x,
            int length) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateNull(String columnName) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBoolean(String columnName, boolean x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateByte(String columnName, byte x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateShort(String columnName, short x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateInt(String columnName, int x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateLong(String columnName, long x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateFloat(String columnName, float x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateDouble(String columnName, double x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBigDecimal(String columnName, BigDecimal x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateString(String columnName, String x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBytes(String columnName, byte[] x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateDate(String columnName, Date x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateTime(String columnName, Time x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateTimestamp(String columnName, Timestamp x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateAsciiStream(String columnName, InputStream x,
            int length) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBinaryStream(String columnName, InputStream x,
            int length) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateCharacterStream(String columnName, Reader reader,
            int length) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateObject(String columnName, Object x, int scale)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void updateObject(String columnName, Object x)
            throws SQLException {
        throw new UnimplementedException();

    }

    public void insertRow() throws SQLException {
        throw new UnimplementedException();

    }

    public void updateRow() throws SQLException {
        throw new UnimplementedException();

    }

    public void deleteRow() throws SQLException {
        throw new UnimplementedException();

    }

    public void refreshRow() throws SQLException {
        throw new UnimplementedException();

    }

    public void cancelRowUpdates() throws SQLException {
        throw new UnimplementedException();

    }

    public void moveToInsertRow() throws SQLException {
        throw new UnimplementedException();

    }

    public void moveToCurrentRow() throws SQLException {
        throw new UnimplementedException();

    }

    public java.sql.Statement getStatement() throws SQLException {
        throw new UnimplementedException();
    }

    public Object getObject(int i, Map map) throws SQLException {
        throw new UnimplementedException();
    }

    public Ref getRef(int i) throws SQLException {
        throw new UnimplementedException();
    }

    public Blob getBlob(int i) throws SQLException {
        throw new UnimplementedException();
    }

    public Clob getClob(int i) throws SQLException {
        throw new UnimplementedException();
    }

    public Array getArray(int i) throws SQLException {
        throw new UnimplementedException();
    }

    public Object getObject(String colName, Map map) throws SQLException {
        throw new UnimplementedException();
    }

    public Ref getRef(String colName) throws SQLException {
        throw new UnimplementedException();
    }

    public Blob getBlob(String colName) throws SQLException {
        throw new UnimplementedException();
    }

    public Clob getClob(String colName) throws SQLException {
        throw new UnimplementedException();
    }

    public Array getArray(String colName) throws SQLException {
        throw new UnimplementedException();
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new UnimplementedException();
    }

    public Date getDate(String columnName, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new UnimplementedException();
    }

    public Time getTime(String columnName, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public Timestamp getTimestamp(String columnName, Calendar cal)
            throws SQLException {
        throw new UnimplementedException();
    }

    public URL getURL(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public URL getURL(String columnName) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateClob(String columnName, Clob x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new UnimplementedException();

    }

    public void updateArray(String columnName, Array x) throws SQLException {
        throw new UnimplementedException();

    }

    public int getHoldability() throws SQLException {
        throw new UnimplementedException();
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new UnimplementedException();
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        throw new UnimplementedException();
    }

    public String getNString(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public String getNString(String columnLabel) throws SQLException {
        throw new UnimplementedException();
    }

    public RowId getRowId(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        throw new UnimplementedException();
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new UnimplementedException();
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isClosed() throws SQLException {
        throw new UnimplementedException();
    }

    public void updateAsciiStream(int columnIndex, InputStream x)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateAsciiStream(String columnLabel, InputStream x)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateBinaryStream(int columnIndex, InputStream x)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateBinaryStream(String columnLabel, InputStream x)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateBinaryStream(String columnLabel, InputStream x,
        long length) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateBlob(int columnIndex, InputStream inputStream)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateBlob(String columnLabel, InputStream inputStream)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateBlob(String columnLabel, InputStream inputStream,
        long length) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateCharacterStream(int columnIndex, Reader x)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateCharacterStream(String columnLabel, Reader reader)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateCharacterStream(String columnLabel, Reader reader,
        long length) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateClob(String columnLabel, Reader reader)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateClob(int columnIndex, Reader reader, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateClob(String columnLabel, Reader reader, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNCharacterStream(int columnIndex, Reader x)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNCharacterStream(String columnLabel, Reader reader)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNCharacterStream(String columnLabel, Reader reader,
        long length) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNClob(int columnIndex, NClob clob) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNClob(String columnLabel, NClob clob) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNClob(String columnLabel, Reader reader)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNClob(int columnIndex, Reader reader, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNClob(String columnLabel, Reader reader, long length)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNString(int columnIndex, String string)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateNString(String columnLabel, String string)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new UnimplementedException();
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject)
        throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnimplementedException();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnimplementedException();
    }

}
