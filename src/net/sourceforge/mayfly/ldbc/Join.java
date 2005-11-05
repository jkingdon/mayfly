package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.util.*;

import java.sql.*;

public class Join extends ValueObject implements FromElement {

    private final FromElement right;
    private final Where condition;
    private final FromElement left;

    public Join(FromElement left, FromElement right, Where condition) {
        this.left = left;
        this.right = right;
        this.condition = condition;
    }

    public Rows tableContents(DataStore store) throws SQLException {
        Rows unfiltered = (Rows) left.tableContents(store).cartesianJoin(right.tableContents(store));
        return (Rows) unfiltered.select(condition);
    }

    public Rows dummyRows(DataStore store) throws SQLException {
        Rows dummyRows = (Rows) left.dummyRows(store).cartesianJoin(right.dummyRows(store));
        dummyRows.select(condition);
        return dummyRows;
    }

}
