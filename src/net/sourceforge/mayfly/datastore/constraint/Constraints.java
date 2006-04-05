package net.sourceforge.mayfly.datastore.constraint;

import java.util.Iterator;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.util.ImmutableList;

public class Constraints {

    private final PrimaryKey primaryKey;
    private final ImmutableList constraints;

    public Constraints(PrimaryKey primaryKey, ImmutableList constraints) {
        this.primaryKey = primaryKey;
        this.constraints = constraints;
    }

    public Constraints() {
        this(null, new ImmutableList());
    }

    public void check(Rows rows, Row newRow) {
        if (primaryKey != null) {
            primaryKey.check(rows, newRow);
        }
        
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            constraint.check(rows, newRow);
        }
    }

}
