package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;

public class NoGroupBy implements Aggregator {

    public ResultRows group(ResultRows rows, Evaluator evaluator, Selected selected) {
        if (isAggregate(selected)) {
            return selected.aggregate(rows);
        }
        return rows;
    }
    
    private boolean isAggregate(Selected selected) {
        String firstColumn = null;
        String firstAggregate = null;

        for (Expression element : selected) {
            if (firstColumn == null) {
                firstColumn = element.firstColumn();
            }
            if (firstAggregate == null) {
                firstAggregate = element.firstAggregate();
            }
            
            if (firstColumn != null && firstAggregate != null) {
                throw new MayflyException(firstColumn + " is a column but " + 
                    firstAggregate + " is an aggregate");
            }
        }
        return firstAggregate != null;
    }

    public ResultRow check(ResultRow dummyRow, Evaluator evaluator, Selected selected) {
        if (isAggregate(selected)) {
            return new ResultRow();
        }
        else {
            return dummyRow;
        }
    }
    
}
