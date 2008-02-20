package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyResultSet;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.Aggregator;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.GroupByCells;
import net.sourceforge.mayfly.evaluation.GroupByKeys;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.condition.And;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.condition.True;
import net.sourceforge.mayfly.evaluation.from.From;
import net.sourceforge.mayfly.evaluation.from.FromElement;
import net.sourceforge.mayfly.evaluation.from.InnerJoin;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.parser.Location;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Select extends Command {

    private final What what;

    /**
     * Can't yet be final, because of {@link #optimize(Evaluator)}.
     */
    private /*final*/ From from;

    public Condition where;

    /**
     * Not immutable, because of {@link GroupByKeys}
     */
    private final Aggregator groupBy;

    private final boolean distinct;

    private final OrderBy orderBy;

    private final Limit limit;

    public final Location location;

    public Select(What what, From from, Condition where, Aggregator groupBy, 
        boolean distinct, OrderBy orderBy, Limit limit, Location location) {
        this.what = what;
        this.from = from;
        this.where = where;
        this.groupBy = groupBy;
        this.distinct = distinct;
        this.orderBy = orderBy;
        this.limit = limit;
        this.location = location;
    }

    public From from() {
        return from;
    }

    @Override
    public MayflyResultSet select(Evaluator evaluator, Cell lastIdentity) {
        Evaluator aliasEvaluator = new AliasEvaluator(what, evaluator);

        optimize(aliasEvaluator);
        ResultRow dummyRow = dummyRow(aliasEvaluator);
        Selected selected = what.selected(dummyRow);

        check(aliasEvaluator, selected, dummyRow);
        ResultRows rows = query(aliasEvaluator, selected);
        return new MayflyResultSet(selected, rows);
    }

    private void check(Evaluator evaluator, Selected selected, ResultRow dummyRow) {
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            Expression element = (Expression) iter.next();
            element.evaluate(dummyRow, evaluator);
        }
        
        where.evaluate(dummyRow, evaluator);
        where.rejectAggregates("WHERE");
        ResultRow groupedDummyRow = groupBy.check(dummyRow, selected);

        ResultRows afterDistinct = 
            distinct(selected, new ResultRows(groupedDummyRow));

        orderBy.check(afterDistinct.singleRow(), groupedDummyRow, 
            dummyRow, evaluator);
        
        if (orderBy.isEmpty() && limit.isSpecified()) {
            throw new MayflyException("Must specify ORDER BY with LIMIT");
        }
    }

    private ResultRow dummyRow(Evaluator evaluator) {
        FromElement element = from.soleElement();
        return element.dummyRow(evaluator);
    }

    ResultRows query(Evaluator evaluator, Selected selected) {
        FromElement element = from.soleElement();
        ResultRows joinedRows = element.tableContents(
            evaluator);

        ResultRows afterWhere = joinedRows.select(where, evaluator);
        
        ResultRows afterGrouping = groupBy.group(afterWhere, selected);

        ResultRows afterDistinct = distinct(selected, afterGrouping);

        ResultRows sorted = orderBy.sort(afterDistinct, what, evaluator);
        return limit.limit(sorted);
    }

    private ResultRows distinct(Selected selected, ResultRows rows) {
        if (!distinct) {
            return rows;
        }

        Set distinctRows = constructDistinctRows(selected, rows);
        
        return distinctRowsToResultRows(selected, distinctRows);
    }

    private ResultRows distinctRowsToResultRows(Selected selected, Set distinctRows) {
        ResultRows result = new ResultRows();
        for (Iterator iter = distinctRows.iterator(); iter.hasNext();) {
            GroupByCells cells = (GroupByCells) iter.next();
            result = result.with(selected.toRow(cells));
        }
        return result;
    }

    private Set constructDistinctRows(Selected selected, ResultRows rows) {
        Set distinctRows = new LinkedHashSet();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ResultRow row = (ResultRow) iter.next();
            GroupByCells cells = selected.evaluateAll(row);
            distinctRows.add(cells);
        }
        return distinctRows;
    }

    @Override
    public UpdateStore update(DataStore store, String schema) {
        throw new MayflyException(
            "SELECT is only available with query, not update");
    }

    public void optimize() {
        optimize(null);
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
    public void optimize(Evaluator evaluator) {
        if (evaluator != null) {
            ResultRow fullDummyRow = dummyRow(0, evaluator);
            where.evaluate(fullDummyRow, evaluator);
        }

        while (from.size() > 1) {
            // x y z -> join(x, y) z
            FromElement first = from.element(0);
            FromElement second = from.element(1);
            
            Condition on = 
                moveWhereToOn(first, second, evaluator);
            InnerJoin explicitJoin = new InnerJoin(first, second, on);

            from = from.without(0).without(0).with(0, explicitJoin);
        }
    }

    ResultRow dummyRow(int index, Evaluator evaluator) {
        ResultRow dummyRow = from.element(index).dummyRow(evaluator);
        if (index >= from.size() - 1) {
            return dummyRow;
        }
        else {
            return dummyRow.combine(
                dummyRow(index + 1, evaluator));
        }
    }

    private Condition moveWhereToOn(
        FromElement first, FromElement second,
        Evaluator evaluator) {
        if (evaluator == null) {
            // For convenience in tests.
            return Condition.TRUE;
        }

        MoveResult result = new MoveResult();
        moveToResult(first, second, evaluator, result, where);
        where = result.nonMovable;
        return result.toBeMoved;
    }

    private void moveToResult(FromElement first, FromElement second, 
        Evaluator evaluator, 
        final MoveResult moveResult, Condition toAnalyze) {
        if (canMove(toAnalyze, first, second, evaluator)) {
            moveResult.toBeMoved = makeAnd(toAnalyze, moveResult.toBeMoved);
        }
        else if (toAnalyze instanceof And) {
            And and = (And) toAnalyze;
            moveToResult(first, second, evaluator, moveResult, and.leftSide);
            moveToResult(first, second, evaluator, moveResult, and.rightSide);
        }
        else {
            moveResult.nonMovable = toAnalyze;
        }
    }
    
    private Condition makeAnd(Condition left, Condition right) {
        // Turn "foo and true" into "foo" (mainly to make unit tests easier).
        if (left instanceof True) {
            return right;
        }
        else if (right instanceof True) {
            return left;
        }
        else {
            return new And(left, right);
        }
    }

    static class MoveResult {
        Condition toBeMoved = Condition.TRUE;
        Condition nonMovable = Condition.TRUE;
    }

    static boolean canMove(Condition condition, 
        FromElement first, FromElement second, 
        Evaluator evaluator) {
        if (condition.firstAggregate() != null) {
            return false;
        }

        InnerJoin join = new InnerJoin(first, second, Condition.TRUE);
        ResultRow partialDummyRow = join.dummyRow(evaluator);
        try {
            condition.check(partialDummyRow);
            return true;
        }
        catch (NoColumn e) {
            return false;
        }
    }

}
