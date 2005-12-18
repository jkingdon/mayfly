package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.where.*;

public class InnerJoin extends Join implements FromElement {

    public InnerJoin(FromElement left, FromElement right, Where condition) {
        super(left, right, condition);
    }

    public Rows tableContents(DataStore store, String currentSchema) {
        Rows unfiltered = (Rows) left.tableContents(store, currentSchema).cartesianJoin(right.tableContents(store, currentSchema));
        return (Rows) unfiltered.select(condition);
    }

}
