package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.constraint.Constraint;

abstract public class UnresolvedConstraint {

    public Constraint resolve(DataStore store, String schema, String table) {
        throw new UnimplementedException();
    }

}
