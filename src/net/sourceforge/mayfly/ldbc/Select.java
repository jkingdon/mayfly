package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

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

        Limit limit =
            converted.selectObjectsThatAre(Limit.class).size() > 0 ?
            (Limit) converted.selectObjectThatIs(Limit.class) :
                Limit.NONE;

        return
            new Select(
                new What(converted.selectObjectsThatAre(WhatElement.class)),
                new From(converted.selectObjectsThatAre(FromElement.class)),
                where,
                orderBy,
                limit
            );
    }


    private What what;
    private From from;
    private Where where;
    private final OrderBy orderBy;
    private final Limit limit;

    public Select(What what, From from, Where where) {
        this(what, from, where, new OrderBy(), Limit.NONE);
    }

    public Select(What what, From from, Where where, OrderBy orderBy, Limit limit) {
        this.what = what;
        this.from = from;
        this.where = where;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    public From from() {
        return from;
    }

    public ResultSet select(final DataStore store) {
        Row dummyRow = dummyRow(store);
        What selected = what.selected(dummyRow);
        check(store, selected, dummyRow);
        return new MayflyResultSet(selected, query(store));
    }

    private void check(final DataStore store, What selected, Row dummyRow) {
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            WhatElement element = (WhatElement) iter.next();
            element.evaluate(dummyRow);
        }

        new Rows(dummyRow).select(where);
        orderBy.check(dummyRow);
        
        if (orderBy.isEmpty() && limit.isSpecified()) {
            throw new MayflyException("Must specify ORDER BY with LIMIT");
        }
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
        Rows sorted = orderBy.sort(store, selected);
        return limit.limit(sorted);
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
