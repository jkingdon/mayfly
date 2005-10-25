package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

import java.sql.*;
import java.util.*;

public final class MyResultSet extends ResultSetStub {
    int pos = -1;

    private final List columnNames;

    private final Rows rows;

    private final Columns columns;

    public MyResultSet(Columns columns, Rows rows) {
        super();
        this.columns = columns;
        this.columnNames = columns.asNames();
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
        checkColumnName(columnName);
        Row row = (Row) rows.element(checkedRowNumber());
        Cell cell = row.cell(new Column(columnName));
        return cell.asInt();
    }

    public int getInt(int oneBasedColumn) throws SQLException {
        int zeroBasedColumn = oneBasedColumn - 1;
        if (zeroBasedColumn < 0 || zeroBasedColumn >= columns.size()) {
            throw new SQLException("no column " + oneBasedColumn);
        }
        Column column = (Column) columns.asImmutableList().get(zeroBasedColumn);
        return getInt(column.columnName());
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
        for (int i = 0; i < columnNames.size(); ++i) {
            String columnName = (String) columnNames.get(i);
            if (target.equalsIgnoreCase(columnName)) {
                return;
            }
        }
        throw new SQLException("no column " + target);
    }

    public void close() throws SQLException {
    }
}