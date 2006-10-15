package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.MayflyResultSet;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.Aggregator;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.GroupByKeys;
import net.sourceforge.mayfly.evaluation.NoGroupBy;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.from.From;
import net.sourceforge.mayfly.evaluation.from.FromElement;
import net.sourceforge.mayfly.evaluation.from.InnerJoin;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;
import net.sourceforge.mayfly.parser.Parser;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

public class Select extends Command {

    private static final String UPDATE_MESSAGE = "SELECT is only available with query, not update";

    public static Select selectFromSql(String sql) {
        return new Parser(sql).parseQuery();
    }

    public static Select selectFromTokens(List tokens) {
        return new Parser(tokens).parseQuery();
    }

    /**
     * Not yet immutable, because of {@link What#add(WhatElement)}
     */
    private final What what;

    /**
     * Not yet immutable, because of {@link From#add(FromElement)}
     */
    private final From from;

    private final BooleanExpression where;

    /**
     * Not yet immutable, because of {@link GroupByKeys}
     */
    private final Aggregator groupBy;
    /** Not yet immutable */
    private final OrderBy orderBy;

    private final Limit limit;

    public Select(What what, From from, BooleanExpression where) {
        this(what, from, where, new NoGroupBy(), new OrderBy(), Limit.NONE);
    }

    public Select(What what, From from, BooleanExpression where, Aggregator groupBy, OrderBy orderBy, Limit limit) {
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
        optimize();
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
        groupBy.check(dummyRow, selected);

        orderBy.check(dummyRow);
        
        if (orderBy.isEmpty() && limit.isSpecified()) {
            throw new MayflyException("Must specify ORDER BY with LIMIT");
        }
    }

    private Row dummyRow(final DataStore store, String currentSchema) {
        FromElement element = soleFromElement();
        Rows joinedRows = element.dummyRows(store, currentSchema);
        if (joinedRows.size() != 1) {
            throw new RuntimeException("internal error: got " + joinedRows.size());
        }
        return (Row) joinedRows.element(0);
    }

    ResultRows query(DataStore store, String currentSchema, Selected selected) {
        FromElement element = soleFromElement();
        Rows joinedRows = element.tableContents(store, currentSchema);

        ResultRows afterWhere = new ResultRows((Rows) joinedRows.select(where));
        
        ResultRows afterGrouping = groupBy.group(afterWhere, selected);

        ResultRows sorted = orderBy.sort(store, afterGrouping, what);
        return limit.limit(sorted);
    }

    private FromElement soleFromElement() {
        if (from.size() != 1) {
            throw new MayflyInternalException("optimizer left us " + from.size() + " elements");
        }

        FromElement element = (FromElement) from.element(0);
        return element;
    }

    public UpdateStore update(DataStore store, String schema) {
        throw new MayflyException(UPDATE_MESSAGE);
    }

    public void optimize() {
        while (from.size() > 1) {
            // x y z -> join(x, y) z
            FromElement first = (FromElement) from.element(0);
            FromElement second = (FromElement) from.element(1);
            
            InnerJoin explicitJoin = new InnerJoin(first, second, BooleanExpression.TRUE);
            from.remove(0);
            from.remove(0);
            from.add(0, explicitJoin);
        }
    }

}
