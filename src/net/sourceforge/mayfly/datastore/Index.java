package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.NullConstraint;
import net.sourceforge.mayfly.datastore.constraint.UniqueConstraint;
import net.sourceforge.mayfly.parser.Location;

public class Index {

    private final String name;
    public final ColumnNames columns;
    private final Constraint constraint;
    public final boolean unique;

    public Index(String name, ColumnNames columns, boolean unique) {
        this.name = name;
        this.columns = columns;
        if (unique) {
            this.constraint = new UniqueConstraint(columns, null);
        }
        else {
            this.constraint = new NullConstraint();
        }
        this.unique = unique;
    }

    public Index(String name, ColumnNames columns) {
        this(name, columns, false);
    }

    public boolean hasName() {
        return name != null;
    }
    
    public String name() {
        return name;
    }

    public Index renameColumn(String oldName, String newName) {
        return new Index(name, columns.renameColumn(oldName, newName));
    }

    public void check(Rows rows, Row newRow, TableReference table, Location location) {
        constraint.check(rows, newRow, table, location);
    }

    public void checkExistingRows(DataStore store, TableReference table) {
        constraint.checkExistingRows(store, table);
    }

}
