package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.constraint.Constraint;

abstract public class UnresolvedConstraint {

    abstract public Constraint resolve(DataStore store, String schema, String table);

    abstract public Constraint resolve(DataStore store, String schema, String table,
        Columns columns);

}
