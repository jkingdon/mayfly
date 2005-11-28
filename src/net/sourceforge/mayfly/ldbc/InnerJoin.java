package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.where.*;

public class InnerJoin extends Join implements FromElement {

    public InnerJoin(FromElement left, FromElement right, Where condition) {
        super(left, right, condition);
    }

    public Rows tableContents(DataStore store) {
        Rows unfiltered = (Rows) left.tableContents(store).cartesianJoin(right.tableContents(store));
        return (Rows) unfiltered.select(condition);
    }

}
