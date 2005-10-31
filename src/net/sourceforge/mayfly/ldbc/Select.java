package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.util.*;

import org.ldbc.parser.*;

import java.sql.*;
import java.util.*;

public class Select extends ValueObject {

    public static Select fromTree(Tree selectTree) {

        int[] typesToIgnore = new int[]{SQLTokenTypes.COMMA};

        L converted =
            selectTree.children().convertUsing(TreeConverters.forSelectTree(), typesToIgnore);

        Where where =
            converted.selectObjectsThatAre(Where.class).size() > 0 ?
                    (Where) converted.selectObjectThatIs(Where.class) :
                    Where.EMPTY;

        return
            new Select(
                new What(converted.selectObjectsThatAre(WhatElement.class)),
                new From(converted.selectObjectsThatAre(FromElement.class)),
                where
            );
    }


    private What what;
    private From from;
    private Where where;

    public Select(What what, From from, Where where) {
        this.what = what;
        this.from = from;
        this.where = where;
    }

    public From from() {
        return from;
    }

    public ResultSet select(final DataStore store) throws SQLException {
        Columns columns = what.selectedColumns();
        checkColumns(store, columns);
        return new MyResultSet(columns, executeOn(store));
    }

    private void checkColumns(final DataStore store, Columns columns) throws SQLException {
        // This method could use some unit testing.
        // And also a comparison to make sure its rules correspond to executeOn
        Set possibleColumnNames = new HashSet();
        for (Iterator iter = from.tableNames().iterator(); iter.hasNext();) {
            String tableName = (String) iter.next();
            possibleColumnNames.addAll(store.table(tableName).columns());
        }
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            String table = column.tableOrAlias(); // we don't do aliases yet
            if (table != null) {
                if (!store.table(table).columns().contains(column)) {
                    throw new SQLException("no column " + table + "." + column.columnName());
                }
            } else {
                if (!possibleColumnNames.contains(column)) {
                    throw new SQLException("no column " + column.columnName());
                }
            }
        }
    }

    public Rows executeOn(DataStore store) throws SQLException {
        L tableNames = from.tableNames();

        Iterator iterator = tableNames.iterator();
        String firstTable = (String) iterator.next();
        Rows joinedRows = store.table(firstTable).rows();
        while (iterator.hasNext()) {
            String tableName = (String) iterator.next();
            joinedRows = (Rows)joinedRows.cartesianJoin(store.table(tableName).rows());
        }

        return (Rows) joinedRows.select(where);
    }
}
