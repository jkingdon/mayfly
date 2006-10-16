package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;

/**
 * Table reference or join.  All implementors should be immutable objects.
 */
public interface FromElement {

    public abstract Rows tableContents(DataStore store, String currentSchema);

    public abstract Row dummyRow(DataStore store, String currentSchema);

}
