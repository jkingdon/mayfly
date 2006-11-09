package net.sourceforge.mayfly.datastore.constraint;

import java.io.IOException;
import java.io.Writer;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.parser.Location;

public class ForeignKey extends Constraint {

    /**
     * @internal
     * True if a foreign key can point to not just a primary key
     * or a column with a unique constraint, but also to a column
     * which itself is a foreign key pointing elsewhere.  True
     * for compatibility with MySQL; false for compatibility with
     * most other databases.
     */
    private static final boolean 
        FOREIGN_KEY_CAN_POINT_TO_FOREIGN_KEY = true;

    private final String referencerSchema;
    private final String referencerTable;
    private final String referencerColumn;

    private final TableReference targetTable;
    private final String targetColumn;

    private final Action onDelete;
    private final Action onUpdate;
    
    public ForeignKey(String referencerTable, String referencerColumn,
        String targetTable, String targetColumn) {
        this(DataStore.ANONYMOUS_SCHEMA_NAME, referencerTable, referencerColumn,
            new TableReference(DataStore.ANONYMOUS_SCHEMA_NAME, targetTable), 
            targetColumn, new NoAction(), new NoAction(), null);
    }

    public ForeignKey(
        String referencerSchema, String referencerTable, String referencerColumn, 
        TableReference targetTable, String targetColumn, 
        Action onDelete, Action onUpdate, String constraintName) {
        super(constraintName);
        
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

    public void checkExistingRows(DataStore store, TableReference table) {
        checkWeAreInTheRightPlace(table.schema(), table.tableName());
        TableData tableData = store.schema(referencerSchema).table(referencerTable);
        for (int i = 0; i < tableData.rowCount(); ++i) {
            Row row = tableData.row(i);
            checkInsert(store, row, Location.UNKNOWN);
        }
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
                if (newPossibleTarget.sqlEquals(value, location)) {
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
     * @internal
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

    /**
     * @internal
     * Doesn't apply for foreign key; instead we check in
     * {@link #checkDelete(DataStore, String, String, Row, Row)}
     * and
     * {@link #checkInsert(DataStore, String, String, Row, Location)}.
     */
    public void check(Rows existingRows, Row proposedRow, Location location) {
    }

    /**
     * @internal
     * For foreign key we currently check in
     * {@link #checkDropReferencerColumn(TableReference, String)}.
     */
    public boolean checkDropColumn(TableReference table, String column) {
        return checkDropReferencerColumn(table, column);
    }
    
    public boolean canBeTargetOfForeignKey(String targetColumn) {
        if (FOREIGN_KEY_CAN_POINT_TO_FOREIGN_KEY) {
            return targetColumn.equalsIgnoreCase(referencerColumn);
        }
        return super.canBeTargetOfForeignKey(targetColumn);
    }

    public void dump(Writer out) throws IOException {
        throw new UnimplementedException();
    }

}
