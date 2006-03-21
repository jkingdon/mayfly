package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;

public class UpdateStore {

    private final DataStore store;
    private final int rowsAffected;

    public UpdateStore(DataStore store, int rowsAffected) {
        this.store = store;
        this.rowsAffected = rowsAffected;
    }

    public DataStore store() {
        return store;
    }

    public int rowsAffected() {
        return rowsAffected;
    }

}
