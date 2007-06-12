package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;

import java.io.IOException;
import java.io.Writer;

public class UniqueConstraint extends NotNullOrUnique {

    public UniqueConstraint(Columns columns) {
        this(columns, null);
    }

    public UniqueConstraint(Columns columns, String constraintName) {
        super(columns, constraintName);
    }

    public UniqueConstraint(ColumnNames columns, String constraintName) {
        super(columns, constraintName);
    }

    protected void checkForNull(String column, Cell proposedCell) {
    }

    public Constraint renameColumn(String oldName, String newName) {
        return new UniqueConstraint(
            names.renameColumn(oldName, newName), 
            constraintName);
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
