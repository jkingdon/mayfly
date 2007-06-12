package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.NullCell;

import java.io.IOException;
import java.io.Writer;

public class PrimaryKey extends NotNullOrUnique {

    public PrimaryKey(Columns columns) {
        this(columns, null);
    }

    public PrimaryKey(Columns columns, String constraintName) {
        super(columns, constraintName);
    }

    public PrimaryKey(ColumnNames columns, String constraintName) {
        super(columns, constraintName);
    }

    protected void checkForNull(String column, Cell proposedCell) {
        if (proposedCell instanceof NullCell) {
            throw new MayflyException("primary key " + column + " cannot be null");
        }
    }

    public Constraint renameColumn(String oldName, String newName) {
        return new PrimaryKey(
            names.renameColumn(oldName, newName), 
            constraintName);
    }

    protected String description() {
        return "primary key";
    }
    
    public void dump(Writer out) throws IOException {
        out.write("PRIMARY KEY(");
        names.dump(out);
        out.write(")");
    }

}
