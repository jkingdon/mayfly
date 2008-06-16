package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.util.ImmutableList;

public class GroupBy implements Aggregator {
    
    private final GroupByKeys keys;
    private final Condition having;

    public GroupBy(ImmutableList<GroupItem> items, Condition having) {
        this(new GroupByKeys(items), having);
    }

    public GroupBy(GroupItem... item) {
        this(ImmutableList.fromArray(item), Condition.TRUE);
    }
    
    public GroupBy(GroupByKeys keys, Condition having) {
        this.keys = keys;
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
    
    public Aggregator resolve(ResultRow afterJoins, Evaluator evaluator) {
        Condition newHaving = having.resolve(afterJoins, evaluator);
        GroupByKeys newKeys = keys.resolve(afterJoins, evaluator);
        if (newHaving != having || newKeys != keys) {
            return new GroupBy(newKeys, newHaving);
        }
        else {
            return this;
        }
    }
    
    public ResultRow check(ResultRow afterJoins, Evaluator evaluator, Selected selected) {
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
