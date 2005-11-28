package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.util.*;

public abstract class Join extends ValueObject implements FromElement {

    protected final FromElement right;
    protected final Where condition;
    protected final FromElement left;

    protected Join(FromElement left, FromElement right, Where condition) {
        this.left = left;
        this.right = right;
        this.condition = condition;
    }

    public Rows dummyRows(DataStore store) {
        Rows dummyRows = (Rows) left.dummyRows(store).cartesianJoin(right.dummyRows(store));
        dummyRows.select(condition);
        return dummyRows;
    }

}
