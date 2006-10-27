package net.sourceforge.mayfly.datastore.constraint;

import java.util.Iterator;
import java.util.List;

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

    public void checkExistingRows(DataStore store) {
        TableData table = store.schema(referencerSchema).table(referencerTable);
        for (int i = 0; i < table.rowCount(); ++i) {
            Row row = table.row(i);
            checkInsert(store, row, Location.UNKNOWN);
        }
    }

    public void checkDuplicates(List keysToCheckAgainst) {
        if (hasForeignKey(constraintName, keysToCheckAgainst)) {
            throw new MayflyException(
                "duplicate constraint name " + constraintName);
        }
    }

    private boolean hasForeignKey(String constraintName, List keys) {
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            ForeignKey key = (ForeignKey) iter.next();
            if (key.nameMatches(constraintName)) {
                return true;
            }
        }
        return false;
    }

    public void checkInsert(DataStore store, String schema, String table, 
        Row proposedRow, Location location) {

        checkWeAreInTheRightPlace(schema, table);

        checkInsert(store, proposedRow, location);
    }

    private void checkInsert(DataStore store, Row proposedRow, Location location) {
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

            throwInsertException(referencerSchema, value, location);
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
        String targetTableName = targetTable.displayName(schema);
        throw new MayflyException("foreign key violation: " + targetTableName + 
            " has no " +
            targetColumn +
            " " + value.asBriefString(),
            location);
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
                TableReference.formatTableName(schema, referencerSchema, referencerTable) +
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
