package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;

public class UniqueConstraint extends NotNullOrUnique {

    public UniqueConstraint(Columns columns) {
        super(columns);
    }

    protected void checkForNull(Column column, Cell proposedCell) {
    }

    protected String description() {
        return "unique column";
    }

}
