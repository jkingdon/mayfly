package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.util.ImmutableList;

public class GroupBy implements Aggregator {
    
    private final GroupByKeys keys;
    private Condition having;

    public GroupBy(ImmutableList<GroupItem> items, Condition having) {
        keys = new GroupByKeys(items);
        this.having = having;
    }

    public GroupBy(GroupItem... item) {
        this(ImmutableList.fromArray(item), Condition.TRUE);
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
        having = having.resolve(afterJoins, evaluator);
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
            having.evaluate(afterJoins, evaluator);
            throw new MayflyException(doesNotSurviveGroupBy.displayName() + 
                " is not aggregate or mentioned in GROUP BY",
                having.location());
        }
    }

}
