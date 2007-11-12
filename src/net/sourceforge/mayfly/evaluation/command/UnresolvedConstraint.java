package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.constraint.Constraint;

abstract public class UnresolvedConstraint {

    abstract public Constraint resolve(DataStore store, String schema, String table);

    public Constraint resolve(ConstraintsBuilder builder) {
        if (builder.columns == null) {
            return resolve(builder.store, builder.schema, builder.table);
        }
        else {
            return resolve(builder.store, builder.schema, builder.table, 
                builder.columns);
        }
    }

    abstract Constraint resolve(DataStore store, String schema, String table,
        Columns columns);

}
