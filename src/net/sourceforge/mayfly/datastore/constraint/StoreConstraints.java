package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Iterator;
import java.util.List;

public class StoreConstraints {
    
    private final ImmutableList constraints;
    
    public StoreConstraints() {
        this(new ImmutableList());
    }

    public StoreConstraints(ImmutableList constraints) {
        this.constraints = constraints;
    }

    public StoreConstraints withAll(List newStoreConstraints) {
        return new StoreConstraints(constraints.withAll(newStoreConstraints));
    }

    public void checkInsert(DataStore store, String schema, String table, Columns columns, List values) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            ForeignKey constraint = (ForeignKey) iter.next();
            constraint.checkInsert(store, schema, table, columns, values);
        }
    }

}
