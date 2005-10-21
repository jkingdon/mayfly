package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.ldbc.*;

import java.sql.*;
import java.util.*;

public class TableData {

    private final ImmutableList columnNames;
    private final ImmutableList rows;

    public TableData(List columnNames) {
        this(new ImmutableList(columnNames), new ImmutableList());
    }
    
    private TableData(ImmutableList columnNames, ImmutableList rows) {
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public int getInt(String columnName, int rowIndex) throws SQLException {
        Map row = (Map) rows.get(rowIndex);
        Long value = (Long) row.get(findColumn(columnName));
        return (int) value.longValue();
    }

    public TableData addRow(List columnNames, List values) throws SQLException {
        Map rowBuilder = new HashMap();
        for (int i = 0; i < columnNames.size(); ++i) {
            rowBuilder.put(findColumn((String) columnNames.get(i)), values.get(i));
        }
        return new TableData(this.columnNames, rows.with(new ImmutableMap(rowBuilder)));
    }

    public String findColumn(String columnName) throws SQLException {
        for (int i = 0; i < columnNames.size(); ++i) {
            String name = (String) columnNames.get(i);
            if (columnName.equalsIgnoreCase(name)) {
                return name;
            }
        }
        throw new SQLException("no column " + columnName);
    }

    public List columnNames() {
        return columnNames;
    }
    
    public int rowCount() {
        return rows.size();
    }

    public Rows rows() {
        return new Rows(rows);
    }

}