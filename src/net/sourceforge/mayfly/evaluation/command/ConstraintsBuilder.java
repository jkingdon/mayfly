package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.datastore.constraint.ForeignKey;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class ConstraintsBuilder {

    public static ConstraintsBuilder fromTable(
        DataStore store, TableReference table) {
        String tableName = table.tableName();
        String namePrefix = tableName + "_ibfk_";
        int nextForeignKeyNumber = 1;

        TableData tableData = store.table(table);
        for (Constraint constraint : tableData.constraints) {
            if (constraint instanceof ForeignKey
                && constraint.constraintName.startsWith(namePrefix)) {
                ++nextForeignKeyNumber;
            }
        }
        return new ConstraintsBuilder(store, table.schema(), tableName, nextForeignKeyNumber);
    }

    private List<Constraint> resolved = new ArrayList<Constraint>();
    
    private int nextForeignKeyNumber = 1;

    public final DataStore store;
    public final String schema;
    public final String table;
    public final Columns columns;

    public ConstraintsBuilder(
        DataStore store, String schema, String table, Columns columns) {
        this.store = store;
        this.schema = schema;
        this.table = table;
        this.columns = columns;
    }
    
    private ConstraintsBuilder(DataStore store, String schema, String table,
        int nextForeignKeyNumber) {
        this.store = store;
        this.schema = schema;
        this.table = table;
        this.nextForeignKeyNumber = nextForeignKeyNumber;
        this.columns = null;
    }

    public void add(Constraint constraint) {
        resolved.add(constraint);
    }

    public Constraints asConstraints() {
        return new Constraints(new ImmutableList(resolved));
    }

    public void add(UnresolvedConstraint constraint) {
        add(constraint.resolve(this));
    }

    public String assignForeignKeyName() {
        return table + "_ibfk_" + nextForeignKeyNumber++;
    }

}
