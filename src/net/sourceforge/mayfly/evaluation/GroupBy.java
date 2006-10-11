package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.Iterator;

public class GroupBy extends ValueObject implements Aggregator {
    
    private GroupByKeys keys = new GroupByKeys();
    private BooleanExpression having = BooleanExpression.TRUE;

    public void add(GroupItem item) {
        keys.add(item);
    }

    public void setHaving(BooleanExpression having) {
        this.having = having;
    }
    
    public GroupedRows makeGroupedRows(Rows rows) {
        return makeGroupedRows(new ResultRows(rows));
    }

    private GroupedRows makeGroupedRows(ResultRows resultRows) {
        GroupedRows grouped = new GroupedRows();
        for (Iterator iter = resultRows.iterator(); iter.hasNext();) {
            ResultRow row = (ResultRow) iter.next();
            grouped.add(keys, row);
        }
        return grouped;
    }

    public ResultRows group(ResultRows rows, Selected selected) {
        ResultRows resultOfGrouping = makeGroupedRows(rows).ungroup(selected);
        return resultOfGrouping.select(having);
    }
    
    public void check(Row dummyRow, Selected selected) {
        ResultRow dummyResultRow = new ResultRow(dummyRow);

        keys.resolve(dummyRow);
        makeGroupedRows(new ResultRows(dummyResultRow)).ungroup(selected);
    }

}
