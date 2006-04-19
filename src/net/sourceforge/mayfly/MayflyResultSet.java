package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.what.Selected;

import java.math.BigDecimal;
import java.sql.SQLException;

public final class MayflyResultSet extends ResultSetStub {

    private int pos = -1;
    private boolean wasNull = false;

    private final Rows rows;
    private final Selected selected;

    public MayflyResultSet(Selected selected, Rows rows) {
        super();
        this.selected = selected;
        this.rows = rows;
    }

    public boolean next() throws SQLException {
        ++pos;
        if (pos >= rows.size()) {
            return false;
        } else {
            return true;
        }
    }

    public byte getByte(String columnName) throws SQLException {
        return cellFromName(columnName).asByte();
    }

    public byte getByte(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asByte();
    }

    public short getShort(String columnName) throws SQLException {
        return cellFromName(columnName).asShort();
    }

    public short getShort(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asShort();
    }

    public int getInt(String columnName) throws SQLException {
        return cellFromName(columnName).asInt();
    }

    public int getInt(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asInt();
    }

    public long getLong(String columnName) throws SQLException {
        try {
            return cellFromName(columnName).asLong();
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    public long getLong(int oneBasedColumn) throws SQLException {
        try {
            return cellFromIndex(oneBasedColumn).asLong();
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return cellFromName(columnName).asBigDecimal();
    }

    public BigDecimal getBigDecimal(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asBigDecimal();
    }

    private static final String SUGGEST_SET_SCALE = "Instead of passing a scale to getBigDecimal, \n" +
        "call getBigDecimal without a scale and then call setScale on the returned BigDecimal";

    /** @internal
     * @deprecated */
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        throw new SQLException(SUGGEST_SET_SCALE);
    }

    /** @internal
     * @deprecated */
    public BigDecimal getBigDecimal(int oneBasedColumn, int scale) throws SQLException {
        throw new SQLException(SUGGEST_SET_SCALE);
    }
    
    public double getDouble(String columnName) throws SQLException {
        return cellFromName(columnName).asDouble();
    }

    public float getFloat(String columnName) throws SQLException {
        /* As float is an inexact type, I think that truncating is
           probably the right thing, rather than throwing an exception
           for values out of range. */
        return (float) cellFromName(columnName).asDouble();
    }

    public double getDouble(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asDouble();
    }

    public float getFloat(int oneBasedColumn) throws SQLException {
        return (float) cellFromIndex(oneBasedColumn).asDouble();
    }

    public String getString(String columnName) throws SQLException {
        return cellFromName(columnName).asString();
    }

    public String getString(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asString();
    }

    public Object getObject(String columnName) throws SQLException {
        try {
            return cellFromName(columnName).asObject();
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    public Object getObject(int oneBasedColumn) throws SQLException {
        try {
            return cellFromIndex(oneBasedColumn).asObject();
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }
    
    public boolean wasNull() throws SQLException {
        return wasNull;
    }

    private Cell cellFromName(String columnName) throws SQLException {
        try {
            return cell(columnFromName(columnName));
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    private Column columnFromName(String columnName) throws SQLException {
        try {
            return currentRow().findColumn(columnName);
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

    private Cell cell(Column column) throws SQLException {
        Cell cell = currentRow().cell(column);
        wasNull = cell instanceof NullCell;
        return cell;
    }

    private Row currentRow() throws SQLException {
        return (Row) rows.element(checkedRowNumber());
    }

    private int checkedRowNumber() throws SQLException {
        if (pos < 0) {
            throw new SQLException("no current result row");
        }
        if (pos >= rows.size()) {
            throw new SQLException("already read last result row");
        }
        return pos;
    }

    public void close() throws SQLException {
    }

}
