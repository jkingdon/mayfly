package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.DataStore;

public interface FromElement {

    public abstract Rows tableContents(DataStore store, String currentSchema);

    public abstract Rows dummyRows(DataStore store, String currentSchema);

}