package net.sourceforge.mayfly.evaluation.expression;

import java.util.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class Count extends AggregateExpression {

    public Count(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }

    protected Cell pickOne(Cell minimum, Cell maximum, Cell count, Cell sum, Cell average) {
        return count;
    }
    
    protected Cell aggregateNonNumeric(Collection values) {
        return new LongCell(values.size());
    }

}
