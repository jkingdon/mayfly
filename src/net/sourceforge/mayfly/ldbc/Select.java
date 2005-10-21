package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;
import org.ldbc.parser.*;

import java.util.*;
import java.sql.*;

public class Select extends ValueObject {

    public static Select fromTree(Tree selectTree) {

        int[] typesToIgnore = new int[]{SQLTokenTypes.COMMA};

        L converted =
            selectTree.children().convertUsing(TreeConverters.forSelectTree(), typesToIgnore);

        return
            new Select(
                new What(converted.selectObjectsThatAre(WhatElement.class)),
                new Froms(converted.selectObjectsThatAre(From.class)),
                (Where) converted.selectObjectThatIs(Where.class)
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

    public Rows executeOn(DataStore store) {
        Rows result = null;

        try {
            L tableNames = froms.tableNames();

            for (Iterator iterator = tableNames.iterator(); iterator.hasNext();) {
                String tableName = (String) iterator.next();
                result = result == null ?
                             store.table(tableName).rows() :
                             result.join(store.table(tableName).rows());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
