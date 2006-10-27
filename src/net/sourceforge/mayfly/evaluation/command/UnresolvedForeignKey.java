package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.datastore.constraint.Action;
import net.sourceforge.mayfly.datastore.constraint.ForeignKey;

public class UnresolvedForeignKey extends UnresolvedConstraint {
    private final String referencingColumn;
    private final UnresolvedTableReference targetTable;
    private final String targetColumn;
    private final Action onDelete;
    private final Action onUpdate;
    final String constraintName;

    public UnresolvedForeignKey(String referencingColumn, 
        UnresolvedTableReference targetTable, String targetColumn, 
        Action onDelete, Action onUpdate, String constraintName) {
        this.referencingColumn = referencingColumn;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
        this.onDelete = onDelete;
        this.onUpdate = onUpdate;
        this.constraintName = constraintName;
    }
    
    public ForeignKey resolve(DataStore store, String schema, String table) {
        TableReference resolvedTargetTable = 
            targetTable.resolve(store, schema, table);
        if (resolvedTargetTable.tableName().equalsIgnoreCase(table)) {
            // self-reference case.  For now, don't worry about primary
            // keys.
        }
        else {
            TableData targetTableData = store.table(resolvedTargetTable);
            if (!targetTableData.hasPrimaryKeyOrUnique(targetColumn)) {
                throw new MayflyException("foreign key refers to " +
                    resolvedTargetTable.displayName(schema) +
                    "(" + targetColumn + ")" +
                    " which is not unique or a primary key"
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
