package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.util.L;

import java.util.Iterator;

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

    public UpdateStore update(DataStore store, String currentSchema) {
        Schema schema = new Schema();
        for (Iterator iter = createTableCommands.iterator(); iter.hasNext();) {
            CreateTable command = (CreateTable) iter.next();
            schema = command.update(schema);
        }
        DataStore newStore = store.addSchema(schemaName, schema);
        return new UpdateStore(newStore, 0);
    }

}
