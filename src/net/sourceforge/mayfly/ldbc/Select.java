package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.util.*;

import org.ldbc.parser.*;

import java.sql.*;
import java.util.*;

public class Select extends Command {

    private static final String UPDATE_MESSAGE = "SELECT is only available with query, not update";

    public static Select selectFromTree(Tree selectTree) {

        int[] typesToIgnore = new int[]{SQLTokenTypes.COMMA};

        L converted =
            selectTree.children().convertUsing(TreeConverters.forSelectTree(), typesToIgnore);

        Where where =
            converted.selectObjectsThatAre(Where.class).size() > 0 ?
                    (Where) converted.selectObjectThatIs(Where.class) :
                    Where.EMPTY;
                    
        OrderBy orderBy =
            converted.selectObjectsThatAre(OrderBy.class).size() > 0 ?
            (OrderBy) converted.selectObjectThatIs(OrderBy.class) :
            new OrderBy();

        return
            new Select(
                new What(converted.selectObjectsThatAre(WhatElement.class)),
                new From(converted.selectObjectsThatAre(FromElement.class)),
                where,
                orderBy
            );
    }


    private What what;
    private From from;
    private Where where;
    private final OrderBy orderBy;

    public Select(What what, From from, Where where) {
        this(what, from, where, new OrderBy());
    }

    public Select(What what, From from, Where where, OrderBy orderBy) {
        this.what = what;
        this.from = from;
        this.where = where;
        this.orderBy = orderBy;
    }

    public From from() {
        return from;
    }

    public ResultSet select(final DataStore store) {
        Columns columns = what.selectedColumns();
        checkColumns(store, columns);
        return new MyResultSet(columns, query(store));
    }

    private void checkColumns(final DataStore store, Columns columns) {
        Row row = dummyRow(store);

        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            row.cell(column);
        }

        new Rows(row).select(where);
    }

    private Row dummyRow(final DataStore store) {
        Iterator iterator = from.iterator();

        FromElement firstTable = (FromElement) iterator.next();
        Rows joinedRows = firstTable.dummyRows(store);
        while (iterator.hasNext()) {
            FromElement table = (FromElement) iterator.next();
            joinedRows = (Rows)joinedRows.cartesianJoin(table.dummyRows(store));
        }
        if (joinedRows.size() != 1) {
            throw new RuntimeException("internal error: got " + joinedRows.size());
        }
        return (Row) joinedRows.element(0);
    }

    public Rows query(DataStore store) {
        Iterator iterator = from.iterator();

        FromElement firstTable = (FromElement) iterator.next();
        Rows joinedRows = firstTable.tableContents(store);
        while (iterator.hasNext()) {
            FromElement table = (FromElement) iterator.next();
            joinedRows = (Rows) joinedRows.cartesianJoin(table.tableContents(store));
        }

        Rows selected = (Rows) joinedRows.select(where);
        return orderBy.sort(store, selected);
    }

    public void substitute(Collection jdbcParameters) {
        Iterator iter = jdbcParameters.iterator();
        what.substitute(iter);
        where.substitute(iter);
    }

    public int parameterCount() {
        return what.parameterCount() + where.parameterCount();
    }

    public DataStore update(DataStore store) {
        throw new UnimplementedException(UPDATE_MESSAGE);
    }

    public int rowsAffected() {
        throw new UnimplementedException(UPDATE_MESSAGE);
    }

}
