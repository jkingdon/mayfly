package net.sourceforge.mayfly.evaluation;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class NoGroupBy extends ValueObject implements Aggregator {

    public Rows group(Rows rows, What what, Selected selected) {
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
                throw new MayflyException(firstColumn + " is a column but " + firstAggregate + " is an aggregate");
            }
        }
        return firstAggregate != null;
    }

    public void check(Row dummyRow, What what) {
    }
    
}
