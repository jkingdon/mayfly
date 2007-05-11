package net.sourceforge.mayfly.datastore.constraint;

import java.io.IOException;
import java.io.Writer;

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
    
    public void dump(Writer out) throws IOException {
        out.write("UNIQUE(");
        names.dump(out);
        out.write(")");
    }

}
