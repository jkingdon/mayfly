package net.sourceforge.mayfly.datastore;

import java.sql.*;
import java.util.*;

public class TableData {

    private List columnNames;
    private List rows;

    public TableData(List columnNames) {
        super();
        this.columnNames = new ArrayList(columnNames);
        this.rows = new ArrayList();
    }

    public int getInt(String columnName, int rowIndex) throws SQLException {
        Map row = (Map) rows.get(rowIndex);
        Long value = (Long) row.get(findColumn(columnName));
        return (int) value.longValue();
    }

    public void addRow(List columnNames, List values) throws SQLException {
        Map row = new HashMap();
        for (int i = 0; i < columnNames.size(); ++i) {
            row.put(findColumn((String) columnNames.get(i)), values.get(i));
        }
        rows.add(row);
    }

    private String findColumn(String columnName) throws SQLException {
        for (int i = 0; i < columnNames.size(); ++i) {
            String name = (String) columnNames.get(i);
            if (columnName.equalsIgnoreCase(name)) {
                return name;
            }
        }
        throw new SQLException("no column " + columnName);
    }

    public List columnNames() {
        return Collections.unmodifiableList(columnNames);
    }
    
    public int rowCount() {
        return rows.size();
    }

}