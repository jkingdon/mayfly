package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.TableReference;

public class ForeignKey {

    private final String referencerSchema;
    private final String referencerTable;
    private final String referencerColumn;

    private final TableReference targetTable;
    private final String targetColumn;

    private final Action onDelete;
    private final Action onUpdate;
    
    public ForeignKey(String referencerTable, String referencerColumn,
        TableReference targetTable, String targetColumn) {
        this(DataStore.ANONYMOUS_SCHEMA_NAME, referencerTable, referencerColumn,
            targetTable, targetColumn, new NoAction(), new NoAction());
    }

    public ForeignKey(
        String referencerSchema, String referencerTable, String referencerColumn, 
        TableReference targetTable, String targetColumn, 
        Action onDelete, Action onUpdate) {
        
        this.referencerSchema = referencerSchema;
        this.referencerTable = referencerTable;
        this.referencerColumn = referencerColumn;

        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
        this.onDelete = onDelete;
        this.onUpdate = onUpdate;
        
        if (onUpdate instanceof Cascade) {
            throw new MayflyException("ON UPDATE CASCADE not implemented");
        }
        else if (onUpdate instanceof SetNull) {
            throw new MayflyException("ON UPDATE SET NULL not implemented");
        }
        else if (onUpdate instanceof SetDefault) {
            throw new MayflyException("ON UPDATE SET DEFAULT not implemented");
        }
    }

    public void checkInsert(DataStore store, String schema, String table, 
        Row proposedRow) {

        if (!referencerSchema.equalsIgnoreCase(schema) || 
            !referencerTable.equalsIgnoreCase(table)) {
            throw new MayflyInternalException(
                "I'm confused about what tables foreign key constraints" +
                " are attached to");
        }

        TableData foundTable = store.table(targetTable);
        Cell value = pickValue(proposedRow);
        if (!(value instanceof NullCell) &&
            !foundTable.hasValue(targetColumn, value)) {
            throwInsertException(schema, value);
        }
    }

    private void throwInsertException(String schema, Cell value) {
        String targetTableName = formatTableName(
            schema, targetTable.schema(), targetTable.tableName());
        throw new MayflyException("foreign key violation: " + targetTableName + 
            " has no " +
            targetColumn +
            " " + value.asBriefString());
    }

    private String formatTableName(
        String defaultSchema, String schemaToFormat, String tableToFormat) {
        StringBuilder result = new StringBuilder();
        if (!schemaToFormat.equalsIgnoreCase(defaultSchema)) {
            result.append(schemaToFormat);
            result.append(".");
        }
        result.append(tableToFormat);
        return result.toString();
    }

    private Cell pickValue(Row proposedRow) {
        Columns columns = proposedRow.columns();
        for (int i = 0; i < columns .size(); ++i) {
            Column column = columns.get(i);
            if (column.matchesName(referencerColumn)) {
                return proposedRow.cell(column);
            }
        }
        throw new MayflyInternalException("Didn't find " + targetColumn + 
            " in " + columns.toString());
    }

    public DataStore checkDelete(DataStore store, String schema, String table, 
        Row rowToDelete, Row replacementRow) {
        if (tableIsMyTarget(schema, table)) {
            Column column = rowToDelete.findColumn(targetColumn);
            Cell oldValue = rowToDelete.cell(column);
            TableData referencer = 
                store.table(referencerSchema, referencerTable);

            if (replacementRow != null) {
                Cell newValue = replacementRow.cell(column);
                if (oldValue.sqlEquals(newValue)) {
                    return store;
                }
                if (referencer.hasValue(referencerColumn, oldValue)) {
                    return onUpdate.handleUpdate(oldValue, newValue, 
                        store, referencerSchema, referencerTable,
                        referencerColumn, targetTable, targetColumn);
                }
            }
            else {
                if (referencer.hasValue(referencerColumn, oldValue)) {
                    return onDelete.handleDelete(oldValue, store, 
                        referencerSchema, referencerTable, referencerColumn,
                        targetTable, targetColumn);
                }
            }
        }
        return store;
    }

    public void checkDropTable(DataStore store, String schema, String table) {
        if (tableIsMyTarget(schema, table)) {
            throw new MayflyException(
                "cannot drop " + table +
                " because a foreign key in table " + 
                formatTableName(schema, referencerSchema, referencerTable) +
                " refers to it"
            );
        }
    }

    private boolean tableIsMyTarget(String schema, String table) {
        return targetTable.matches(schema, table);
    }

}
