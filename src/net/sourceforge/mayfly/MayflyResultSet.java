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

    public String getString(String columnName) throws SQLException {
        return cellFromName(columnName).asString();
    }

    public String getString(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asString();
    }

    public Object getObject(String columnName) throws SQLException {
        return cellFromName(columnName).asObject();
    }

    public Object getObject(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asObject();
    }
    
    public boolean wasNull() throws SQLException {
        return wasNull;
    }

    private Cell cellFromName(String columnName) throws SQLException {
        return cell(columnFromName(columnName));
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
