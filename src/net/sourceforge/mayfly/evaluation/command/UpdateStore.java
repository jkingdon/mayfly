package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;

public class UpdateStore {

    private final DataStore store;
    private final int rowsAffected;
    public final Cell newIdentityValue;

    public UpdateStore(DataStore store, int rowsAffected) {
        this(store, rowsAffected, null);
    }

    public UpdateStore(DataStore store, int rowsAffected, 
        Cell newIdentityValue) {
        this.store = store;
        this.rowsAffected = rowsAffected;
        this.newIdentityValue = newIdentityValue;
    }

    public DataStore store() {
        return store;
    }

    public int rowsAffected() {
        return rowsAffected;
    }

}
