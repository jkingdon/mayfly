package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.constraint.CheckConstraint;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.evaluation.condition.Condition;

public class UnresolvedCheckConstraint extends UnresolvedConstraint {

    private final String constraintName;
    private final Condition condition;

    public UnresolvedCheckConstraint(
        Condition condition, String constraintName) {
        this.condition = condition;
        this.constraintName = constraintName;
    }

    public Constraint resolve(DataStore store, String schema, String table) {
        throw new UnimplementedException();
    }

    public Constraint resolve(DataStore store, String schema, String table,
        Columns columns) {
        return new CheckConstraint(condition, table, constraintName);
    }

}
