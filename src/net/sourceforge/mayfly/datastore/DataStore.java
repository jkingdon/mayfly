package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.*;

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
        return new DataStore(tables.with(table, new TableData(Columns.fromColumnNames(table, columnNames))));
    }

    public DataStore dropTable(String table) {
        String canonicalTableName = lookUpTable(table);
        return new DataStore(tables.without(canonicalTableName));
    }

    public TableData table(String table) {
        String canonicalTableName = lookUpTable(table);
        
        return (TableData) tables.get(canonicalTableName);
    }

    private String lookUpTable(String target) {
        for (Iterator iter = tables.keySet().iterator(); iter.hasNext(); ) {
            String canonicalTable = (String) iter.next();
            if (canonicalTable.equalsIgnoreCase(target)) {
                return canonicalTable;
            }
        }
        throw new MayflyException("no table " + target);
    }

    public Set tables() {
        return tables.keySet();
    }

    public DataStore addRow(String table, List columnNames, List values) {
        return new DataStore(tables.with(lookUpTable(table), table(table).addRow(columnNames, values)));
    }

    public DataStore addRow(String table, List values) {
        return new DataStore(tables.with(lookUpTable(table), table(table).addRow(values)));
    }

}
