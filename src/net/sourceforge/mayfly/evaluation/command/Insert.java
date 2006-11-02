package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

public class Insert extends Command {

    public final UnresolvedTableReference table;
    public final ImmutableList columnNames;
    public final ValueList values;
    public final Location location;

    public Insert(UnresolvedTableReference table, 
        ImmutableList columnNames, ValueList values,
        Location location) {
        this.table = table;
        this.columnNames = columnNames;
        this.values = values;
        this.location = location;
    }

    public String table() {
        return table.tableName();
    }

    public UpdateStore update(DataStore store, String currentSchema) {
        String schema = schemaToUse(currentSchema);
        Checker checker = new Checker(store, schema, table(), location);

        return new UpdateStore(
            insertOneRow(store, schema, checker),
            1,
            checker.newIdentityValue()
        );
    }

    private DataStore insertOneRow(DataStore store, String schema, Checker checker) {
        if (columnNames == null) {
            return store.addRow(schema, table(), values, checker);
        }
        else {
            return store.addRow(schema, table(), 
                columnNames, values, checker);
        }
    }

    private String schemaToUse(String currentSchema) {
        return table.schema(currentSchema);
    }

}
