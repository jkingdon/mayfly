package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;

public class GroupBy implements Aggregator {
    
    private GroupByKeys keys = new GroupByKeys();
    private Condition having = Condition.TRUE;

    public void add(GroupItem item) {
        keys.add(item);
    }

    public void setHaving(Condition having) {
        this.having = having;
    }
    
    public GroupedRows makeGroupedRows(ResultRows resultRows, Evaluator evaluator) {
        GroupedRows grouped = new GroupedRows();
        for (ResultRow row : resultRows) {
            grouped.add(keys, row, evaluator);
        }
        return grouped;
    }

    public ResultRows group(ResultRows rows, Evaluator evaluator, Selected selected) {
        ResultRows resultOfGrouping = makeGroupedRows(rows, evaluator).ungroup(selected);
        return resultOfGrouping.select(having, evaluator);
    }
    
    public ResultRow check(ResultRow afterJoins, Evaluator evaluator, Selected selected) {
        keys.resolve(afterJoins, evaluator);

        GroupedRows grouped = makeGroupedRows(new ResultRows(afterJoins), evaluator);
        ResultRows resultOfGrouping = grouped.ungroup(selected);

        ResultRow afterGroupBy = resultOfGrouping.singleRow();
        checkHaving(afterJoins, afterGroupBy, evaluator);
        return afterGroupBy;
    }

    private void checkHaving(ResultRow afterJoins, ResultRow afterGroupBy,
        Evaluator evaluator) {
        try {
            having.evaluate(afterGroupBy, evaluator);
        }
        catch (NoColumn doesNotSurviveGroupBy) {
            if (having.isAggregate()) {
                throw new UnimplementedException(
                    "aggregates in HAVING not yet fully implemented");
            }

            having.evaluate(afterJoins, evaluator);
            throw new MayflyException(doesNotSurviveGroupBy.displayName() + 
                " is not aggregate or mentioned in GROUP BY",
                having.location());
        }
    }

}
