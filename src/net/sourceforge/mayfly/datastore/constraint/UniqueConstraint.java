package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Columns;

public class UniqueConstraint extends NotNullOrUnique {

    public UniqueConstraint(Columns columns) {
        this(columns, null);
    }

    public UniqueConstraint(Columns columns, String constraintName) {
        super(columns, constraintName);
    }

    protected void checkForNull(String column, Cell proposedCell) {
    }

    protected String description() {
        return "unique column";
    }

}
