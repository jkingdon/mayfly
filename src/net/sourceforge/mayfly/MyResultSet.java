package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

import java.sql.*;

public final class MyResultSet extends ResultSetStub {
    int pos = -1;

    private final Rows rows;

    private final Columns columns;

    public MyResultSet(Columns columns, Rows rows) {
        super();
        this.columns = columns;
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

    public int getInt(String columnName) throws SQLException {
        return cellFromName(columnName).asInt();
    }

    public int getInt(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asInt();
    }

    public String getString(String columnName) throws SQLException {
        return cellFromName(columnName).asString();
    }

    public String getString(int oneBasedColumn) throws SQLException {
        return cellFromIndex(oneBasedColumn).asString();
    }

    private Cell cellFromName(String columnName) throws SQLException {
        return cell(columnFromName(columnName));
    }

    private Column columnFromName(String columnName) throws SQLException {
        checkColumnName(columnName);
        return new Column(columnName);
    }

    private Cell cellFromIndex(int oneBasedColumn) throws SQLException {
        return cell(columnFromIndex(oneBasedColumn));
    }

    private Column columnFromIndex(int oneBasedColumn) throws SQLException {
        int zeroBasedColumn = oneBasedColumn - 1;
        if (zeroBasedColumn < 0 || zeroBasedColumn >= columns.size()) {
            throw new SQLException("no column " + oneBasedColumn);
        }
        return columns.get(zeroBasedColumn);
    }

    private Cell cell(Column column) throws SQLException {
        Row row = (Row) rows.element(checkedRowNumber());
        return row.cell(column);
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

    private void checkColumnName(String target) throws SQLException {
        for (int i = 0; i < columns.size(); ++i) {
            Column column = columns.get(i);
            if (column.matchesName(target)) {
                return;
            }
        }
        throw new SQLException("no column " + target);
    }

    public void close() throws SQLException {
    }
}