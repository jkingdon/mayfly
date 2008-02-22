package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;

public class NoopCommand extends Command {

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        return new UpdateStore(store, 0);
    }

}
