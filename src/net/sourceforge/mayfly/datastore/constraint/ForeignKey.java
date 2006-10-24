package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.parser.Location;

public class ForeignKey {

    private final String referencerSchema;
    private final String referencerTable;
    private final String referencerColumn;

    private final TableReference targetTable;
    private final String targetColumn;

    private final Action onDelete;
    private final Action onUpdate;
    private final String constraintName;
    
    public ForeignKey(String referencerTable, String referencerColumn,
        TableReference targetTable, String targetColumn) {
        this(DataStore.ANONYMOUS_SCHEMA_NAME, referencerTable, referencerColumn,
            targetTable, targetColumn, new NoAction(), new NoAction(), null);
    }

    public ForeignKey(
        String referencerSchema, String referencerTable, String referencerColumn, 
        TableReference targetTable, String targetColumn, 
        Action onDelete, Action onUpdate, String constraintName) {
        
        this.referencerSchema = referencerSchema;
        this.referencerTable = referencerTable;
        this.referencerColumn = referencerColumn;

        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
        this.onDelete = onDelete;
        this.onUpdate = onUpdate;
        this.constraintName = constraintName;
        
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
        Row proposedRow, Location location) {

        checkWeAreInTheRightPlace(schema, table);

        TableData foundTable = store.table(targetTable);
        Cell value = proposedRow.cell(referencerColumn);
        if (!(value instanceof NullCell) &&
            !foundTable.hasValue(targetColumn, value)) {

            /*
               Check for the case in which the row we are in the
               process of inserting satisfies the constraint.
             */
            if (targetTable.matches(referencerSchema, referencerTable)) {
                Cell newPossibleTarget = proposedRow.cell(targetColumn);
                if (newPossibleTarget.sqlEquals(value)) {
                    return;
                }
            }

            throwInsertException(schema, value, location);
        }
    }

    private void checkWeAreInTheRightPlace(String schema, String table) {
        if (!referencerSchema.equalsIgnoreCase(schema) || 
            !referencerTable.equalsIgnoreCase(table)) {
            throw new MayflyInternalException(
                "I'm confused about what tables foreign key constraints" +
                " are attached to");
        }
    }

    private void throwInsertException(String schema, Cell value, Location location) {
        String targetTableName = formatTableName(
            schema, targetTable.schema(), targetTable.tableName());
        throw new MayflyException("foreign key violation: " + targetTableName + 
            " has no " +
            targetColumn +
            " " + value.asBriefString(),
            location);
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

    public DataStore checkDelete(DataStore store, String schema, String table, 
        Row rowToDelete, Row replacementRow) {
        if (tableIsMyTarget(schema, table)) {
            Cell oldValue = rowToDelete.cell(targetColumn);
            TableData referencer = 
                store.table(referencerSchema, referencerTable);

            if (replacementRow != null) {
                Cell newValue = replacementRow.cell(targetColumn);
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

    /**
     * @returns should we keep this key?
     */
    public boolean checkDropReferencerColumn(TableReference table, String column) {
        checkWeAreInTheRightPlace(table.schema(), table.tableName());
        if (column.equalsIgnoreCase(referencerColumn)) {
            // I think we just need to return false to make this
            // work.  Need to write that test.
            throw new MayflyException(
                "mayfly does not currently allow dropping a " +
                "column with a foreign key (table " +
                referencerTable + ", column " + referencerColumn + ")");
        }
        return true;
    }

    public void checkDropTargetColumn(TableReference table, String column) {
        if (tableIsMyTarget(table.schema(), table.tableName()) &&
            column.equalsIgnoreCase(targetColumn)) {
            throw new MayflyException("the column " + column + 
                " is referenced by a foreign key in table " + 
                //formatTableName(defaultSchema, referencerSchema, referencerTable)
                referencerTable +
                ", column " + referencerColumn
                );
        }
    }

    public boolean nameMatches(String target) {
        if (constraintName == null) {
            return false;
        }
        return this.constraintName.equalsIgnoreCase(target);
    }

}
