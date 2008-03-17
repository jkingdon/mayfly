package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.command.UpdateSchema;
import net.sourceforge.mayfly.evaluation.command.UpdateTable;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.ImmutableMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Schema {

    private final ImmutableMap<String, TableData> tables;
    
    public Schema() {
        this(new ImmutableMap());
    }

    private Schema(ImmutableMap<String, TableData> tables) {
        this.tables = tables;
    }

    public Schema createTable(String table, List columnNames) {
        return createTable(
            table, 
            Columns.fromColumnNames(columnNames), 
            new Constraints(),
            new ImmutableList());
    }

    public Schema createTable(
        String table, Columns columns, Constraints constraints,
        ImmutableList indexes) {
        assertNoTable(table);
        return new Schema(tables.with(table, 
            new TableData(columns, constraints, indexes)));
    }

    public Schema addColumn(String table, Column newColumn, Position position) {
        TableData oldTable = table(table);
        return new Schema(tables.with(table, 
            oldTable.addColumn(newColumn, position)));
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

    public Schema renameColumn(String table, String oldName, String newName) {
        TableData oldTable = table(table);
        return new Schema(
            tables.with(table, oldTable.renameColumn(oldName, newName)));
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
        
        return tables.get(canonicalTableName);
    }

    public String lookUpTable(String target) {
        return lookUpTable(target, Location.UNKNOWN);
    }

    public String lookUpTable(String target, Location location) {
        return lookUpTable(target, location, new Options());
    }

    public String lookUpTable(String target, Location location, Options options) {
        for (String canonicalTable : tables.keySet()) {
            if (canonicalTable.equalsIgnoreCase(target)) {
                if (options.tableNamesCaseSensitive()
                    && !canonicalTable.equals(target)) {
                    throw new MayflyException(
                        "attempt to refer to table " + canonicalTable + 
                        " as " + target + 
                        " (with case sensitive table names enabled)",
                        location);
                }
                else {
                    return canonicalTable;
                }
            }
        }
        throw new MayflyException("no table " + target, location);
    }

    private String lookUpTableOrNull(String target) {
        return lookUpTableOrNull(target, new Options());
    }

    private String lookUpTableOrNull(String target, Options options) {
        for (String canonicalTable : tables.keySet()) {
            if (options.tableNamesEqual(canonicalTable, target)) {
                return canonicalTable;
            }
        }
        return null;
    }

    public Set tables() {
        return tables.keySet();
    }

    public Schema addRow(Checker checker, TableReference table, 
        List columnNames, ValueList values) {
        return new Schema(tables.with(lookUpTable(table.tableName()), 
            table(table.tableName()).addRow(checker, table, columnNames, values)));
    }
    
    public Schema addRow(String table, List columnNames, ValueList values) {
        return addRow(new NullChecker(), 
            new TableReference(DataStore.ANONYMOUS_SCHEMA_NAME, table), 
            columnNames, values);
    }

    public Schema addRow(Checker checker, TableReference table, ValueList values) {
        return new Schema(
            tables.with(
                lookUpTable(table.tableName()), 
                table(table.tableName()).addRow(checker, table, values)));
    }

    public UpdateSchema update(Checker checker, TableReference table, 
        List setClauses, Condition where) {
        UpdateTable result = 
            table(table.tableName())
                .update(checker, setClauses, where, table);
        return replaceTable(table.tableName(), result);
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
        for (TableData potentialReferencer : tables.values()) {
            potentialReferencer.checkDropTable(store, schema, table);
        }
    }

    public Schema dropForeignKey(String table, String constraintName) {
        return replaceTable(table, table(table).dropForeignKey(constraintName));
    }

    public Schema dropConstraint(String table, String constraintName) {
        return replaceTable(table, table(table).dropConstraint(constraintName));
    }

    public Schema addConstraint(String table, Constraint constraint) {
        return replaceTable(table, table(table).addConstraint(constraint));
    }

    public Schema addIndex(String table, Index index) {
        String foundTable = findTableForIndex(index.name());
        if (foundTable != null) {
            throw new MayflyException(
                "table " + foundTable + 
                " already has an index " + index.name());
        }
        return replaceTable(table, table(table).addIndex(index));
    }

    public Schema dropIndex(String table, String indexName) {
        if (table == null) {
            throw new NullPointerException("table expected");
        }

        String foundTable = findTableForIndex(indexName);
        if (table.equalsIgnoreCase(foundTable)) {
            return replaceTable(table, table(table).dropIndex(indexName));
        }
        else {
            throw new MayflyException(
                "attempt to drop index " + indexName + " from table " + 
                table + " although the index is on table " + foundTable);
        }
    }
    
    public Schema dropIndex(String indexName) {
        String table = findTableForIndex(indexName);
        if (table == null) {
            throw new MayflyException("no index " + indexName);
        }
        return replaceTable(table, table(table).dropIndex(indexName));
    }

    public String findTableForIndex(String indexName) {
        for (Map.Entry<String, TableData> entry : tables.entrySet()) {
            if (entry.getValue().indexes.hasIndex(indexName)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
