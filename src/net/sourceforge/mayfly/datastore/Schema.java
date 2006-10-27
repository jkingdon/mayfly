package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.command.UpdateSchema;
import net.sourceforge.mayfly.evaluation.command.UpdateTable;
import net.sourceforge.mayfly.evaluation.condition.Condition;
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
        return createTable(
            table, 
            Columns.fromColumnNames(columnNames), 
            new Constraints());
    }

    public Schema createTable(
        String table, Columns columns, Constraints constraints) {
        assertNoTable(table);
        return new Schema(tables.with(table, new TableData(columns, constraints)));
    }

    public Schema addColumn(String table, Column newColumn) {
        TableData oldTable = table(table);
        return new Schema(tables.with(table, oldTable.addColumn(newColumn)));
    }

    public Schema dropColumn(TableReference table, String column) {
        TableData oldTable = table(table.tableName());
        return new Schema(
            tables.with(
                table.tableName(), 
                oldTable.dropColumn(table, column)
            ));
    }

    public void checkDropColumn(TableReference table, String column) {
        for (Iterator iter = tables.values().iterator(); iter.hasNext();) {
            TableData potentialReferencer = (TableData) iter.next();
            potentialReferencer.checkDropColumn(table, column);
        }
    }

    public Schema modifyColumn(String tableName, Column newColumn) {
        TableData oldTable = table(tableName);
        return new Schema(
            tables.with(tableName, oldTable.modifyColumn(newColumn)));
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

    public Schema dropTable(Checker checker, String table) {
        String canonicalTableName = lookUpTable(table);
        checker.checkDropTable();
        return new Schema(tables.without(canonicalTableName));
    }

    public TableData table(String table) {
        String canonicalTableName = lookUpTable(table);
        
        return (TableData) tables.get(canonicalTableName);
    }

    public String lookUpTable(String target) {
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

    public Schema addRow(Checker checker, String table, List columnNames, ValueList values) {
        return new Schema(tables.with(lookUpTable(table), 
            table(table).addRow(checker, columnNames, values)));
    }
    
    public Schema addRow(String table, List columnNames, ValueList values) {
        return addRow(new NullChecker(), table, columnNames, values);
    }

    public Schema addRow(Checker checker, String table, ValueList values) {
        return new Schema(
            tables.with(
                lookUpTable(table), 
                table(table).addRow(checker, values)));
    }

    public UpdateSchema update(Checker checker, String table, 
        List setClauses, Condition where) {
        UpdateTable result = table(table).update(checker, setClauses, where, table);
        return replaceTable(table, result);
    }

    public UpdateSchema delete(String table, Condition where, Checker checker) {
        UpdateTable result = table(table).delete(where, checker, table);
        ImmutableMap tablesAfterChecking =
            checker.store().schema(checker.schema()).tables;
        
        /**
         * Here we merge the tables: the one corresponding to table was returned
         * by delete, and the rest come in via the checker.
         * This way the checker is the only thing which operates across
         * tables - the regular code just affects the one.
         */
        Schema schema = new Schema(
            tablesAfterChecking.with(lookUpTable(table), result.table())
        );
        return new UpdateSchema(schema, result.rowsAffected());
    }

    private UpdateSchema replaceTable(String table, UpdateTable result) {
        Schema schema = replaceTable(table, result.table());
        return new UpdateSchema(schema, result.rowsAffected());
    }

    private Schema replaceTable(String tableName, TableData table) {
        return new Schema(tables.with(lookUpTable(tableName), table));
    }

    public DataStore checkDelete(
        DataStore store,
        String schema, String table, Row rowToDelete, Row replacementRow) {
        for (Iterator iter = tables.values().iterator(); iter.hasNext();) {
            TableData potentialReferencer = (TableData) iter.next();
            store = potentialReferencer.checkDelete(
                store,
                schema, table, rowToDelete, replacementRow
            );
        }
        return store;
    }

    public void checkDropTable(DataStore store, String schema, String table) {
        for (Iterator iter = tables.values().iterator(); iter.hasNext();) {
            TableData potentialReferencer = (TableData) iter.next();
            potentialReferencer.checkDropTable(store, schema, table);
        }
    }

    public Schema dropForeignKey(String table, String constraintName) {
        return replaceTable(table, table(table).dropForeignKey(constraintName));
    }

    public Schema addConstraint(String table, Constraint key) {
        return replaceTable(table, table(table).addConstraint(key));
    }

}
