package net.sourceforge.mayfly.evaluation.select;

import java.util.Iterator;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.Aggregator;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.condition.And;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.condition.True;
import net.sourceforge.mayfly.evaluation.from.From;
import net.sourceforge.mayfly.evaluation.from.FromElement;
import net.sourceforge.mayfly.evaluation.from.InnerJoin;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.What;

/* At least currently, this is written as a mutable object.  It is a short-lived
   one.  */
public class Planner {

    private final What what;

    // mutated as we make joins explicit
    private From from;

    // mutated as we move conditions from WHERE to ON
    private Condition where;

    private final Aggregator groupBy;
    private final Distinct distinct;
    private final OrderBy orderBy;
    private final Limit limit;

    public Planner(What what, From from, Condition where, Aggregator groupBy,
        Distinct distinct, OrderBy orderBy, Limit limit) {
        this.what = what;
        this.from = from;
        this.where = where;
        this.groupBy = groupBy;
        this.distinct = distinct;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    public OptimizedSelect planForTests() {
        optimize();
        return new OptimizedSelect(
            null, null, null,
            from.soleElement(), where, groupBy, distinct, orderBy, what, limit);
    }

    public OptimizedSelect plan(Evaluator evaluator) {
        Evaluator aliasEvaluator = new AliasEvaluator(what, evaluator);

        optimize(aliasEvaluator);
        ResultRow dummyRow = dummyRow(aliasEvaluator);
        Selected selected = what.selected(dummyRow);

        check(aliasEvaluator, selected, dummyRow);
        return new OptimizedSelect(
            aliasEvaluator, selected, dummyRow,
            from.soleElement(), where, groupBy, distinct, orderBy, what, limit);
    }

    private void check(Evaluator evaluator, Selected selected, ResultRow dummyRow) {
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            Expression element = (Expression) iter.next();
            element.evaluate(dummyRow, evaluator);
        }
        
        where.evaluate(dummyRow, evaluator);
        where.rejectAggregates("WHERE");
        ResultRow groupedDummyRow = groupBy.check(dummyRow, evaluator, selected);

        ResultRows afterDistinct = 
            distinct.distinct(selected, new ResultRows(groupedDummyRow));

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
