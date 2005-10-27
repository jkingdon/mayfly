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
        return new DataStore(tables.with(table, new TableData(columnNames)));
    }

    public DataStore dropTable(String table) throws SQLException {
        String canonicalTableName = lookUpTable(table);
        return new DataStore(tables.without(canonicalTableName));
    }

    public TableData table(String table) throws SQLException {
        String canonicalTableName = lookUpTable(table);
        
        return (TableData) tables.get(canonicalTableName);
    }

    private String lookUpTable(String target) throws SQLException {
        for (Iterator iter = tables.keySet().iterator(); iter.hasNext(); ) {
            String canonicalTable = (String) iter.next();
            if (canonicalTable.equalsIgnoreCase(target)) {
                return canonicalTable;
            }
        }
        throw new SQLException("no such table " + target);
    }

    public Set tables() {
        return tables.keySet();
    }

    public DataStore addRow(String table, List columnNames, List values) throws SQLException {
        return new DataStore(tables.with(lookUpTable(table), table(table).addRow(columnNames, values)));
    }

}
