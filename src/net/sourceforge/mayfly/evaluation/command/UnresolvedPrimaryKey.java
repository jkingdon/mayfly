package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.PrimaryKey;

public class UnresolvedPrimaryKey extends UnresolvedConstraint {
    
    private final ColumnNames columns;

    private final String constraintName;

    public UnresolvedPrimaryKey(ColumnNames columns, String constraintName) {
        this.columns = columns;
        this.constraintName = constraintName;
    }
    
    public UnresolvedPrimaryKey(String column) {
        this(ColumnNames.singleton(column), null);
    }

    public Constraint resolve(DataStore store, String schema, String table) {
        // Not suitable for CREATE TABLE, because we are assuming
        // the columns are already in the store.
        TableData existingTable = store.schema(schema).table(table);
        if (existingTable.hasPrimaryKey()) {
            throw new MayflyException(
                "attempt to define more than one primary key for table " + table);
        }
        return resolve(store, schema, table, existingTable.columns());
    }
    
    public Constraint resolve(
        DataStore store, String schema, String table, Columns tableColumns) {
        return new PrimaryKey(this.columns.resolve(tableColumns), constraintName);
    }

}
