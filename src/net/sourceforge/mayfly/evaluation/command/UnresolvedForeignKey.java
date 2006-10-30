package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.datastore.constraint.Action;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.ForeignKey;
import net.sourceforge.mayfly.parser.Location;

public class UnresolvedForeignKey extends UnresolvedConstraint {
    private final String referencingColumn;
    private final UnresolvedTableReference targetTable;
    private final String targetColumn;
    private final Action onDelete;
    private final Action onUpdate;
    private final String constraintName;
    private final Location location;

    public UnresolvedForeignKey(String referencingColumn, 
        UnresolvedTableReference targetTable, String targetColumn, 
        Action onDelete, Action onUpdate, String constraintName,
        Location location) {
        this.referencingColumn = referencingColumn;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
        this.onDelete = onDelete;
        this.onUpdate = onUpdate;
        this.constraintName = constraintName;
        this.location = location;
    }
    
    public Constraint resolve(DataStore store, String schema, String table,
        Columns columns) {
        columns.columnFromName(referencingColumn, location);
        return resolve(store, schema, table);
    }

    public Constraint resolve(DataStore store, String schema, String table) {
        TableReference resolvedTargetTable = 
            targetTable.resolve(store, schema, table);
        if (resolvedTargetTable.tableName().equalsIgnoreCase(table)) {
            // self-reference case.  For now, don't worry about primary
            // keys.
        }
        else {
            TableData targetTableData = store.table(resolvedTargetTable);
            if (!targetTableData.canBeTargetOfForeignKey(targetColumn)) {
                throw new MayflyException("foreign key refers to " +
                    resolvedTargetTable.displayName(schema) +
                    "(" + targetColumn + ")" +
                    " which is not unique or a primary key",
                    location
                );
            }
        }

        return new ForeignKey(
            schema,
            table,
            referencingColumn,

            resolvedTargetTable,
            targetColumn,
            
            onDelete,
            onUpdate,
            
            constraintName
        );
    }

}
