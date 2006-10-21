package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;

/**
 * Table reference or join.  All implementors should be immutable objects.
 */
public interface FromElement {

    public abstract ResultRows tableContents(DataStore store, String currentSchema);

    public abstract ResultRow dummyRow(DataStore store, String currentSchema);

}
