package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.Iterator;

public class NoGroupBy extends ValueObject implements Aggregator {

    public ResultRows group(ResultRows rows, Selected selected) {
        if (isAggregate(selected)) {
            return selected.aggregate(rows);
        }
        return rows;
    }
    
    private boolean isAggregate(Selected selected) {
        String firstColumn = null;
        String firstAggregate = null;

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            Expression element = (Expression) iter.next();
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

    public void check(ResultRow dummyRow, Selected selected) {
    }
    
}
