package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class CreateSchema extends Command {

    private final String schemaName;
    private L createTableCommands;

    public CreateSchema(String schemaName) {
        this.schemaName = schemaName;
        createTableCommands = new L();
    }

    public void add(CreateTable command) {
        createTableCommands.add(command);
    }

    public void substitute(Collection jdbcParameters) {
        throw new UnimplementedException();
    }

    public int parameterCount() {
        throw new UnimplementedException();
    }

    public DataStore update(DataStore store, String currentSchema) {
        Schema schema = new Schema();
        for (Iterator iter = createTableCommands.iterator(); iter.hasNext();) {
            CreateTable command = (CreateTable) iter.next();
            schema = command.update(schema);
        }
        return store.addSchema(schemaName, schema);
    }

    public int rowsAffected() {
        return 0;
    }

}
