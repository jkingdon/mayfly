package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.util.*;

import java.sql.*;

public class Join extends ValueObject implements FromElement {

    /*private*/ final FromElement right;
    /*private*/ final Where condition;
    /*private*/ final FromElement left;

    public Join(FromElement left, FromElement right, Where condition) {
        this.left = left;
        this.right = right;
        this.condition = condition;
    }

    public Rows tableContents(DataStore store) throws SQLException {
        throw new UnimplementedException();
    }

    public Rows dummyRows(DataStore store) throws SQLException {
        throw new UnimplementedException();
    }

}
