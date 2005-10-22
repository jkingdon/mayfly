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
                    new Where();

        return
            new Select(
                new What(converted.selectObjectsThatAre(WhatElement.class)),
                new Froms(converted.selectObjectsThatAre(From.class)),
                where
            );
    }


    private What what;
    private Froms froms;
    private Where where;

    public Select(What what, Froms froms, Where where) {
        this.what = what;
        this.froms = froms;
        this.where = where;
    }

    public Froms from() {
        return froms;
    }

    public ResultSet select(DataStore dataStore) throws SQLException {
        TableData tableData = dataStore.table(froms.singleTableName());

        List canonicalizedColumnNames = what.selectedColumns(tableData);
        return new MyResultSet(canonicalizedColumnNames, tableData);
    }

    public Rows executeOn(DataStore store) throws SQLException {
        Rows result = null;

        L tableNames = froms.tableNames();

        for (Iterator iterator = tableNames.iterator(); iterator.hasNext();) {
            String tableName = (String) iterator.next();
            result = result == null ?
                         store.table(tableName).rows() :
                         (Rows)result.cartesianJoin(store.table(tableName).rows());
        }

        return result;
    }
}
