package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.UniqueConstraint;

public class UnresolvedUniqueConstraint extends UnresolvedConstraint {

    private final ColumnNames constraintColumns;
    private final String constraintName;

    public UnresolvedUniqueConstraint(ColumnNames columns, String constraintName) {
        this.constraintName = constraintName;
        this.constraintColumns = columns;
    }

    public UnresolvedUniqueConstraint(String column) {
        this(ColumnNames.singleton(column), null);
    }

    private UniqueConstraint resolve(Columns tableColumns) {
        return new UniqueConstraint(
            constraintColumns.resolve(tableColumns),
            constraintName);
    }
    
    public Constraint resolve(DataStore store, String schema, String table) {
        // Not suitable for CREATE TABLE, because we are assuming
        // the columns are already in the store.
        return resolve(store.schema(schema).table(table).columns());
    }
    
    public Constraint resolve(
        DataStore store, String schema, String table, Columns columns) {
        return resolve(columns);
    }

}
