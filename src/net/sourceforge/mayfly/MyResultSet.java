package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.*;

import java.sql.*;
import java.util.*;

public final class MyResultSet extends ResultSetStub {
    int pos = -1;

    private final List canonicalizedColumnNames;

    private final TableData tableData;

    public MyResultSet(List canonicalizedColumnNames, TableData tableData) {
        super();
        this.canonicalizedColumnNames = canonicalizedColumnNames;
        this.tableData = tableData;
    }

    public boolean next() throws SQLException {
        ++pos;
        if (pos >= tableData.rowCount()) {
            return false;
        } else {
            return true;
        }
    }

    public int getInt(String columnName) throws SQLException {
        String canonicalizedColumnName = lookUpColumn(canonicalizedColumnNames, columnName);
        
        return tableData.getInt(canonicalizedColumnName, checkedRowNumber());
    }

    public int getInt(int oneBasedColumn) throws SQLException {
        int zeroBasedColumn = oneBasedColumn - 1;
        if (zeroBasedColumn < 0 || zeroBasedColumn >= canonicalizedColumnNames.size()) {
            throw new SQLException("no column " + oneBasedColumn);
        }
        String columnName = (String) canonicalizedColumnNames.get(zeroBasedColumn);
        return tableData.getInt(columnName, checkedRowNumber());
    }

    private int checkedRowNumber() throws SQLException {
        if (pos < 0) {
            throw new SQLException("no current result row");
        }
        if (pos >= tableData.rowCount()) {
            throw new SQLException("already read last result row");
        }
        return pos;
    }

    private String lookUpColumn(List canonicalizedColumnNames, String target) throws SQLException {
        for (int i = 0; i < canonicalizedColumnNames.size(); ++i) {
            String columnName = (String) canonicalizedColumnNames.get(i);
            if (target.equalsIgnoreCase(columnName)) {
                return columnName;
            }
        }
        throw new SQLException("no column " + target);
    }

    public void close() throws SQLException {
    }
}