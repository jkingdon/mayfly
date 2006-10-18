package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyResultSet;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.Aggregator;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.GroupByKeys;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.NoGroupBy;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.from.From;
import net.sourceforge.mayfly.evaluation.from.FromElement;
import net.sourceforge.mayfly.evaluation.from.InnerJoin;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.parser.Parser;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

public class Select extends Command {

    private static final String UPDATE_MESSAGE = 
        "SELECT is only available with query, not update";

    public static Select selectFromSql(String sql) {
        return new Parser(sql).parseQuery();
    }

    public static Select selectFromTokens(List tokens) {
        return new Parser(tokens).parseQuery();
    }

    /**
     * Not immutable, because of {@link What#add(WhatElement)}
     */
    private final What what;

    /**
     * Not immutable, because of {@link From#add(FromElement)}
     */
    private final From from;

    public Condition where;

    /**
     * Not immutable, because of {@link GroupByKeys}
     */
    private final Aggregator groupBy;
    /** Not immutable */
    private final OrderBy orderBy;

    private final Limit limit;

    public Select(What what, From from, Condition where) {
        this(what, from, where, new NoGroupBy(), new OrderBy(), Limit.NONE);
    }

    public Select(What what, From from, Condition where, Aggregator groupBy, OrderBy orderBy, Limit limit) {
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
        optimize(store, currentSchema);
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
        FromElement element = from.soleElement();
        return element.dummyRow(store, currentSchema);
    }

    ResultRows query(DataStore store, String currentSchema, Selected selected) {
        FromElement element = from.soleElement();
        Rows joinedRows = element.tableContents(store, currentSchema);

        ResultRows afterWhere = new ResultRows((Rows) joinedRows.select(where));
        
        ResultRows afterGrouping = groupBy.group(afterWhere, selected);

        ResultRows sorted = orderBy.sort(store, afterGrouping, what);
        return limit.limit(sorted);
    }

    public UpdateStore update(DataStore store, String schema) {
        throw new MayflyException(UPDATE_MESSAGE);
    }

    public void optimize() {
        optimize(null, null);
    }

    /**
     * Currently this method makes joins explicit and also moves
     * conditions from WHERE to ON.  The whole thing would probably
     * be cleaner if those were separated.  The second step
     * would be optional (for those tests currently passing in
     * null for store) and the {@link #dummyRow(int, DataStore, String)}
     * method could make use of the joins which were built up
     * in the first step.
     */
    public void optimize(DataStore store, String currentSchema) {
        if (store != null) {
            Row fullDummyRow = dummyRow(0, store, currentSchema);
            where.evaluate(fullDummyRow);
        }

        while (from.size() > 1) {
            // x y z -> join(x, y) z
            FromElement first = (FromElement) from.element(0);
            FromElement second = (FromElement) from.element(1);
            
            Condition on = 
                moveWhereToOn(first, second, store, currentSchema);
            InnerJoin explicitJoin = new InnerJoin(first, second, on);

            from.remove(0);
            from.remove(0);
            from.add(0, explicitJoin);
        }
    }

    Row dummyRow(int index, DataStore store, String currentSchema) {
        FromElement element = (FromElement) from.element(index);
        Row dummyRow = element.dummyRow(store, currentSchema);
        if (index >= from.size() - 1) {
            return dummyRow;
        }
        else {
            return dummyRow.combine(
                dummyRow(index + 1, store, currentSchema));
        }
    }

    private Condition moveWhereToOn(
        FromElement first, FromElement second,
        DataStore store, String currentSchema) {
        if (store == null) {
            // For convenience in tests.
            return Condition.TRUE;
        }

        if (canMove(where, first, second, store, currentSchema)) {
            Condition conditionToMove = where;
            where = Condition.TRUE;
            return conditionToMove;
        }
        else {
            return Condition.TRUE;
        }
    }

    static boolean canMove(Condition condition, 
        FromElement first, FromElement second, 
        DataStore store, String currentSchema) {
        if (condition.firstAggregate() != null) {
            return false;
        }

        InnerJoin join = new InnerJoin(first, second, Condition.TRUE);
        Row partialDummyRow = join.dummyRow(store, currentSchema);
        try {
            condition.check(new ResultRow(partialDummyRow));
            return true;
        }
        catch (NoColumn e) {
            return false;
        }
    }

}
