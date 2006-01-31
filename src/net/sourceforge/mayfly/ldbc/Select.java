package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.*;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.parser.*;

import java.sql.*;
import java.util.*;

public class Select extends Command {

    private static final String UPDATE_MESSAGE = "SELECT is only available with query, not update";

    public static Select selectFromSql(String sql) {
        return new Parser(sql).parseQuery();
    }

    public static Select selectFromTokens(List tokens) {
        return new Parser(tokens).parseQuery();
    }

    private final What what;
    private final From from;
    private final Where where;
    private final Aggregator groupBy;
    private final OrderBy orderBy;
    private final Limit limit;

    public Select(What what, From from, Where where) {
        this(what, from, where, new NoGroupBy(), new OrderBy(), Limit.NONE);
    }

    public Select(What what, From from, Where where, Aggregator groupBy, OrderBy orderBy, Limit limit) {
        this.what = what;
        this.from = from;
        this.where = where;
        this.groupBy = groupBy;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    public From from() {
        return from;
    }

    public ResultSet select(final DataStore store, String currentSchema) {
        Row dummyRow = dummyRow(store, currentSchema);
        Selected selected = what.selected(dummyRow);
        check(store, selected, dummyRow);
        return new MayflyResultSet(selected, query(store, currentSchema, selected));
    }

    private void check(final DataStore store, Selected selected, Row dummyRow) {
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            Expression element = (Expression) iter.next();
            element.evaluate(dummyRow);
        }

        new Rows(dummyRow).select(where);
        String firstAggregate = where.firstAggregate();
        if (firstAggregate != null) {
            throw new MayflyException("aggregate " + firstAggregate + " not valid in WHERE");
        }
        groupBy.check(dummyRow, what);

        orderBy.check(dummyRow);
        
        if (orderBy.isEmpty() && limit.isSpecified()) {
            throw new MayflyException("Must specify ORDER BY with LIMIT");
        }
    }

    private Row dummyRow(final DataStore store, String currentSchema) {
        Iterator iterator = from.iterator();

        FromElement firstTable = (FromElement) iterator.next();
        Rows joinedRows = firstTable.dummyRows(store, currentSchema);
        while (iterator.hasNext()) {
            FromElement table = (FromElement) iterator.next();
            joinedRows = (Rows)joinedRows.cartesianJoin(table.dummyRows(store, currentSchema));
        }
        if (joinedRows.size() != 1) {
            throw new RuntimeException("internal error: got " + joinedRows.size());
        }
        return (Row) joinedRows.element(0);
    }

    Rows query(DataStore store, String currentSchema, Selected selected) {
        Iterator iterator = from.iterator();

        FromElement firstTable = (FromElement) iterator.next();
        Rows joinedRows = firstTable.tableContents(store, currentSchema);
        while (iterator.hasNext()) {
            FromElement table = (FromElement) iterator.next();
            joinedRows = (Rows) joinedRows.cartesianJoin(table.tableContents(store, currentSchema));
        }

        Rows afterWhere = (Rows) joinedRows.select(where);

        Rows afterGrouping = groupBy.group(afterWhere, what, selected);

        Rows sorted = orderBy.sort(store, afterGrouping, what);
        return limit.limit(sorted);
    }

    public DataStore update(DataStore store, String schema) {
        throw new UnimplementedException(UPDATE_MESSAGE);
    }

    public int rowsAffected() {
        throw new UnimplementedException(UPDATE_MESSAGE);
    }

}
