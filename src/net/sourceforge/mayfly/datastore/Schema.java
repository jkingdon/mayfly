package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.command.UpdateSchema;
import net.sourceforge.mayfly.evaluation.command.UpdateTable;
import net.sourceforge.mayfly.ldbc.where.Where;
import net.sourceforge.mayfly.util.ImmutableMap;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Schema {

    private final ImmutableMap tables;
    
    public Schema() {
        this(new ImmutableMap());
    }

    private Schema(ImmutableMap tables) {
        this.tables = tables;
    }

    public Schema createTable(String table, List columnNames) {
        return createTable(table, Columns.fromColumnNames(table, columnNames), new Constraints());
    }

    public Schema createTable(String table, Columns columns, Constraints constraints) {
        assertNoTable(table);
        return new Schema(tables.with(table, new TableData(columns, constraints)));
    }

    private void assertNoTable(String table) {
        String existingTable = lookUpTableOrNull(table);
        if (existingTable != null) {
            throw new MayflyException("table " + existingTable + " already exists");
        }
    }

    public boolean hasTable(String table) {
        return lookUpTableOrNull(table) != null;
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
        String canonicalName = lookUpTableOrNull(target);
        if (canonicalName == null) {
            throw new MayflyException("no table " + target);
        }
        else {
            return canonicalName;
        }
    }

    private String lookUpTableOrNull(String target) {
        for (Iterator iter = tables.keySet().iterator(); iter.hasNext(); ) {
            String canonicalTable = (String) iter.next();
            if (canonicalTable.equalsIgnoreCase(target)) {
                return canonicalTable;
            }
        }
        return null;
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

    public UpdateSchema update(String table, List setClauses, Where where) {
        UpdateTable result = table(table).update(setClauses, where);
        return replaceTable(table, result);
    }

    public UpdateSchema delete(String table, Where where) {
        UpdateTable result = table(table).delete(where);
        return replaceTable(table, result);
    }

    private UpdateSchema replaceTable(String table, UpdateTable result) {
        Schema schema = new Schema(tables.with(lookUpTable(table), result.table()));
        return new UpdateSchema(schema, result.rowsAffected());
    }

}
