package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.what.What;
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
        GroupedRows grouped = new GroupedRows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            grouped.add(keys, row);
        }
        return grouped;
    }

    public Rows group(Rows rows, What what, Selected selected) {
        Rows resultOfGrouping = makeGroupedRows(rows).ungroup(selected);
        return (Rows) resultOfGrouping.select(having);
    }
    
    public void check(Row dummyRow, Selected selected) {
        keys.resolve(dummyRow);
        makeGroupedRows(new Rows(dummyRow)).ungroup(selected);
    }

}
