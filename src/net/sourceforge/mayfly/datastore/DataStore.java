package net.sourceforge.mayfly.datastore;

import java.sql.*;
import java.util.*;

public class DataStore {

    private Map tables = new HashMap();

    public DataStore createTable(String table, List columnNames) {
        tables.put(table.toLowerCase(), new TableData(columnNames));
        return this;
    }

    public DataStore dropTable(String table) throws SQLException {
        if (tables.containsKey(table.toLowerCase())) {
            tables.remove(table.toLowerCase());
        } else {
            throw new SQLException("no such table " + table);
        }
        return this;
    }

    public TableData table(String table) throws SQLException {
        if (tables.containsKey(table.toLowerCase())) {
            return (TableData) tables.get(table.toLowerCase());
        } else {
            throw new SQLException("no such table " + table);
        }
    }

    public Set tables() {
        return Collections.unmodifiableSet(tables.keySet());
    }

    public DataStore addRow(String table, List columnNames, List values) throws SQLException {
        TableData tableData = table(table);
        tableData.addRow(columnNames, values);
        return this;
    }

}
