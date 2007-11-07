package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.DataStore;

public class SetSchema extends Command {

    private final String name;

    public SetSchema(String name) {
        this.name = name;
    }

    @Override
    public UpdateStore update(DataStore store, String schema) {
        throw new MayflyInternalException(
            "set schema doesn't operate on a data store");
    }

    public String name() {
        return name;
    }

}
