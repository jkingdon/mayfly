package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.parser.Location;

import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public final class MayflyResultSet extends ResultSetStub {

    private int pos = -1;
    private boolean wasNull = false;

    private final ResultRows rows;
    private final Selected selected;

    public MayflyResultSet(Selected selected, ResultRows rows) {
        super();
        this.selected = selected;
        this.rows = rows;
    }

    @Override
    public boolean next() {
        ++pos;
        if (pos >= rows.size()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public byte getByte(String columnName) throws SQLException {
        return cellFromName(columnName).asByte();
    }

    @Override
    public byte getByte(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asByte();
    }

    @Override
    public short getShort(String columnName) throws SQLException {
        return cellFromName(columnName).asShort();
    }

    @Override
    public short getShort(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asShort();
    }

    @Override
    public int getInt(String columnName) throws SQLException {
        return cellFromName(columnName).asInt();
    }

    @Override
    public int getInt(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asInt();
    }

    @Override
    public long getLong(String columnName) throws SQLException {
        try {
            return cellFromName(columnName).asLong();
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    @Override
    public long getLong(int oneBasedColumn) throws SQLException {
        try {
            return cellFromIndex(oneBasedColumn).asLong();
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    @Override
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return cellFromName(columnName).asBigDecimal();
    }

    @Override
    public BigDecimal getBigDecimal(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asBigDecimal();
    }

    private static final String SUGGEST_SET_SCALE = 
        "Instead of passing a scale to getBigDecimal, \n" +
        "call getBigDecimal without a scale and then call setScale on the returned BigDecimal";

    /** @internal
     * @deprecated */
    @Override
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        throw new SQLException(SUGGEST_SET_SCALE);
    }

    /** @internal
     * @deprecated */
    @Override
    public BigDecimal getBigDecimal(int oneBasedColumn, int scale) throws SQLException {
        throw new SQLException(SUGGEST_SET_SCALE);
    }
    
    @Override
    public double getDouble(String columnName) throws SQLException {
        return cellFromName(columnName).asDouble();
    }

    @Override
    public float getFloat(String columnName) throws SQLException {
        /* As float is an inexact type, I think that truncating is
           probably the right thing, rather than throwing an exception
           for values out of range. */
        return (float) cellFromName(columnName).asDouble();
    }

    @Override
    public double getDouble(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asDouble();
    }

    @Override
    public float getFloat(int oneBasedColumn) throws SQLException {
        return (float) cellFromIndex(oneBasedColumn).asDouble();
    }

    @Override
    public String getString(String columnName) throws SQLException {
        try {
            return cellFromName(columnName).asString();
        }
        catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    @Override
    public String getString(int oneBasedColumn) throws SQLException {
        try {
            return cellFromIndex(oneBasedColumn).asString();
        }
        catch (MayflyException e) {
            throw e.asSqlException();
        }
    }
    
    @Override
    public Reader getCharacterStream(String columnName) throws SQLException {
        return new StringReader(getString(columnName));
    }
    
    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return new StringReader(getString(columnIndex));
    }
    
    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return cellFromIndex(columnIndex).asBinaryStream();
    }
    
    @Override
    public InputStream getBinaryStream(String columnName) throws SQLException {
        return cellFromName(columnName).asBinaryStream();
    }
    
    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return cellFromIndex(columnIndex).asBlob();
    }
    
    @Override
    public Blob getBlob(String columnName) throws SQLException {
        return cellFromName(columnName).asBlob();
    }
    
    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return cellFromIndex(columnIndex).asBytes();
    }
    
    @Override
    public byte[] getBytes(String columnName) throws SQLException {
        return cellFromName(columnName).asBytes();
    }
    
    private static final String SUGGEST_GET_CHARACTER_STREAM = 
        "We suggest getCharacterStream instead";

    /**
     * @deprecated
     */
    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new UnimplementedException(SUGGEST_GET_CHARACTER_STREAM);
    }
    
    /**
     * @deprecated
     */
    @Override
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        throw new UnimplementedException(SUGGEST_GET_CHARACTER_STREAM);
    }
    
    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnimplementedException(SUGGEST_GET_CHARACTER_STREAM);
    }
    
    @Override
    public InputStream getAsciiStream(String columnName) throws SQLException {
        throw new UnimplementedException(SUGGEST_GET_CHARACTER_STREAM);
    }
    
    @Override
    public java.sql.Date getDate(String columnName, Calendar calendar)
    throws SQLException {
        return cellFromName(columnName).asDate(timeZone(calendar));
    }
    
    @Override
    public java.sql.Date getDate(String columnName) throws SQLException {
        return cellFromName(columnName).asDate(DateTimeZone.getDefault());
    }

    @Override
    public java.sql.Date getDate(int oneBasedColumn, Calendar calendar)
    throws SQLException {
        return cellFromIndex(oneBasedColumn).asDate(timeZone(calendar));
    }

    @Override
    public java.sql.Date getDate(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asDate(DateTimeZone.getDefault());
    }
    
    @Override
    public Timestamp getTimestamp(String columnName) throws SQLException {
        return cellFromName(columnName).asTimestamp(DateTimeZone.getDefault());
    }

    @Override
    public Timestamp getTimestamp(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn)
            .asTimestamp(DateTimeZone.getDefault());
    }
    
    private DateTimeZone timeZone(Calendar calendar) {
        return DateTimeZone.forTimeZone(calendar.getTimeZone());
    }

    @Override
    public Object getObject(String columnName) throws SQLException {
        try {
            return cellFromName(columnName).asObject();
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    @Override
    public Object getObject(int oneBasedColumn) throws SQLException {
        try {
            return cellFromIndex(oneBasedColumn).asObject();
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return wasNull;
    }

    private Cell cellFromName(String columnName) throws SQLException {
        try {
            Cell cell = selected.evaluate(columnName, currentRow());
            wasNull = cell instanceof NullCell;
            return cell;
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    private Cell cellFromIndex(int oneBasedColumn) throws SQLException {
        try {
            Cell cell = selected.evaluate(oneBasedColumn, currentRow());
            wasNull = cell instanceof NullCell;
            return cell;
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    private ResultRow currentRow() throws MayflyException {
        return rows.row(checkedRowNumber());
    }

    private int checkedRowNumber() throws MayflyException {
        if (pos < 0) {
            throw new MayflyException("no current result row");
        }
        if (pos >= rows.size()) {
            throw new MayflyException("already read last result row");
        }
        return pos;
    }

    @Override
    public void close() throws SQLException {
    }

    public Cell scalar() {
        return scalar(Location.UNKNOWN);
    }

    /**
     * @internal
     * Return the result of a query which returns just a single cell.
     */
    public Cell scalar(Location location) {
        checkOneColumn(location);
        
        if (rows.size() == 0) {
            return NullCell.INSTANCE;
        }
        else if (rows.size() != 1) {
            throw new MayflyException(
                "subselect expects one row but got " + rows.size(),
                location);
        }
        
        return rows.row(0).cell(0);
    }
    
    /**
     * @internal
     * Return the single cell from the current row.
     */
    public Cell singleColumn(Location location) {
        checkOneColumn(location);
        
        return currentRow().cell(0);
    }

    private void checkOneColumn(Location location) {
        if (selected.size() != 1) {
            throw new MayflyException(
                "attempt to specify " + selected.size() + 
                " expressions in a subselect", 
                location);
        }
    }

    /**
     * @internal
     * Return the values from the current row.
     */
    public ValueList asValues(Location location) {
        ResultRow row = currentRow();

        ValueList values = new ValueList(location);
        for (int zeroBasedColumn = 0; 
            zeroBasedColumn < selected.size(); 
            ++zeroBasedColumn) {
            Cell cell = selected.evaluate(zeroBasedColumn + 1, row);
            values = values.with(new Value(cell, location));
        }
        return values;
    }

}
