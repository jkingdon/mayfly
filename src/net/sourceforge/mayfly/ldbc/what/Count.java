package net.sourceforge.mayfly.ldbc.what;

import java.util.*;

import net.sourceforge.mayfly.datastore.*;

public class Count extends AggregateExpression {

    public Count(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }

    protected Cell pickOne(Cell min, Cell max, Cell count, Cell sum, Cell average) {
        return count;
    }
    
    protected Cell aggregateNonNumeric(Collection values) {
        return new LongCell(values.size());
    }

}
