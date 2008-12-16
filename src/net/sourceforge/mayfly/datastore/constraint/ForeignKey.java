package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

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
    final String referencerTable;
    final String referencerColumn;

    final TableReference targetTable;
    final String targetColumn;

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

    @Override
    public void checkExistingRows(DataStore store, TableReference table) {
        checkWeAreInTheRightPlace(table.schema(), table.tableName());
        TableData tableData = store.schema(table.schema()).table(table.tableName());
        for (int i = 0; i < tableData.rowCount(); ++i) {
            Row row = tableData.row(i);
            checkInsert(store, row, Location.UNKNOWN);
        }
    }

    @Override
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
            if (refersToSameTable()) {
                Cell newPossibleTarget = proposedRow.cell(targetColumn);
                if (newPossibleTarget.sqlEquals(value, location)) {
                    return;
                }
            }

            throwInsertException(referencerSchema, value, location);
        }
    }
    
    /**
     * @internal
     * Return -1 if first must be inserted before second,
     * 1 if second must be inserted before first,
     * or 0 if they can be inserted in either order (as
     * far as can be determined by just looking at those two).
     */
    @Override
    public boolean mustInsertBefore(Row first, Row second) {
        if (first == second) {
            // A row which satisfies its own constraint is OK.
            return false;
        }

        if (refersToSameTable()) {
            if (second.cell(referencerColumn)
                .sqlEquals(first.cell(targetColumn))) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    private boolean refersToSameTable() {
        return targetTable.matches(referencerSchema, referencerTable);
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
        throw new MayflyException("foreign key violation: attempt in table " + 
            referencerDisplayName(schema) + ", column " + referencerColumn +
            " to reference non-present value " + value.asBriefString() +
            " in table " + targetTableName + 
            ", column " +
            targetColumn,
            location);
    }

    @Override
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

    @Override
    public void checkDropTable(DataStore store, String schema, String table) {
        if (tableIsMyTarget(schema, table) && !refersToSameTable()) {
            throw new MayflyException(
                "cannot drop " + table +
                " because a foreign key in table " + 
                referencerDisplayName(schema) +
                " refers to it"
            );
        }
    }

    private String referencerDisplayName(String schema) {
        return TableReference.formatTableName(schema, referencerSchema, referencerTable);
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
        if (refersTo(column)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean refersTo(String column) {
        return column.equalsIgnoreCase(referencerColumn);
    }

    @Override
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
    
    @Override
    public Constraint renameColumn(String oldName, String newName) {
        if (oldName.equalsIgnoreCase(referencerColumn)) {
            return new ForeignKey(
                referencerSchema, referencerTable, newName, 
                targetTable, targetColumn, 
                onDelete, onUpdate, constraintName);
        }
        else {
            return this;
        }
    }
    
    @Override
    public Constraint renameTable(String oldName, String newName) {
        if (oldName.equalsIgnoreCase(referencerTable)) {
            return new ForeignKey(
                referencerSchema, newName, referencerColumn, 
                targetTable, targetColumn, 
                onDelete, onUpdate, constraintName);
        }
        else {
            return this;
        }
    }
    
    /**
     * @internal
     * Doesn't apply for foreign key; instead we check in
     * {@link #checkDelete(DataStore, String, String, Row, Row)}
     * and
     * {@link #checkInsert(DataStore, String, String, Row, Location)}.
     */
    @Override
    public void check(Rows existingRows, Row proposedRow, 
        TableReference table, Location location) {
    }

    /**
     * @internal
     * For foreign key we currently check in
     * {@link #checkDropReferencerColumn(TableReference, String)}.
     */
    @Override
    public boolean checkDropColumn(TableReference table, String column) {
        return checkDropReferencerColumn(table, column);
    }
    
    @Override
    public boolean canBeTargetOfForeignKey(String targetColumn) {
        if (FOREIGN_KEY_CAN_POINT_TO_FOREIGN_KEY) {
            return targetColumn.equalsIgnoreCase(referencerColumn);
        }
        return super.canBeTargetOfForeignKey(targetColumn);
    }
    
    @Override
    public boolean refersTo(String table, Evaluator evaluator) {
        return targetTable.matches(DataStore.ANONYMOUS_SCHEMA_NAME, table);
    }
    
    @Override
    public List referencedTables() {
        if (refersToSameTable()) {
            return Collections.EMPTY_LIST;
        }
        else {
            return Collections.singletonList(targetTable.tableName());
        }
    }

    @Override
    public void dump(Writer out) throws IOException {
        out.write("FOREIGN KEY(");
        out.write(referencerColumn);
        out.write(") REFERENCES ");
        out.write(targetTable.displayName(DataStore.ANONYMOUS_SCHEMA_NAME));
        out.write("(");
        out.write(targetColumn);
        out.write(")");
        if (!(onDelete instanceof NoAction)) {
            out.write(" ON DELETE ");
            onDelete.dump(out);
        }
    }

}
