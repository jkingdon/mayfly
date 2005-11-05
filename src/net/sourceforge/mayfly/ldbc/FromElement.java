package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;

import java.sql.*;

public interface FromElement {

    public abstract Rows tableContents(DataStore store) throws SQLException;

    public abstract Rows dummyRows(DataStore store) throws SQLException;

}