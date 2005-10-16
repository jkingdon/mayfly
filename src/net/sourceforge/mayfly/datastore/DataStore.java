package net.sourceforge.mayfly.datastore;

import java.sql.*;
import java.util.*;

public class DataStore {

    private final ImmutableMap tables;
    
    public DataStore() {
        this(new ImmutableMap());
    }

    public DataStore(ImmutableMap tables) {
        this.tables = tables;
    }

    public DataStore createTable(String table, List columnNames) {
        return new DataStore(tables.with(table.toLowerCase(), new TableData(columnNames)));
    }

    public DataStore dropTable(String table) throws SQLException {
        if (tables.containsKey(table.toLowerCase())) {
            return new DataStore(tables.without(table.toLowerCase()));
        } else {
            throw new SQLException("no such table " + table);
        }
    }

    public TableData table(String table) throws SQLException {
        if (tables.containsKey(table.toLowerCase())) {
            return (TableData) tables.get(table.toLowerCase());
        } else {
            throw new SQLException("no such table " + table);
        }
    }

    public Set tables() {
        return tables.keySet();
    }

    public DataStore addRow(String table, List columnNames, List values) throws SQLException {
        return new DataStore(tables.with(table.toLowerCase(), table(table).addRow(columnNames, values)));
    }

}
