package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Schema {

    private final ImmutableMap tables;
    
    public Schema() {
        this(new ImmutableMap());
    }

    private Schema(ImmutableMap tables) {
        this.tables = tables;
    }

    public Schema createTable(String table, List columnNames) {
        return new Schema(tables.with(table, new TableData(Columns.fromColumnNames(table, columnNames))));
    }

    public Schema dropTable(String table) {
        String canonicalTableName = lookUpTable(table);
        return new Schema(tables.without(canonicalTableName));
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

    public Schema addRow(String table, List columnNames, List values) {
        return new Schema(tables.with(lookUpTable(table), table(table).addRow(columnNames, values)));
    }

    public Schema addRow(String table, List values) {
        return new Schema(tables.with(lookUpTable(table), table(table).addRow(values)));
    }

}
