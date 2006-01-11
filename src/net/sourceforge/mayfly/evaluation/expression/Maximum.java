package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class Maximum extends AggregateExpression {

    public Maximum(SingleColumn column, String spellingOfMax, boolean distinct) {
        super(column, spellingOfMax, distinct);
    }

    protected Cell pickOne(Cell minimum, Cell maximum, Cell count, Cell sum, Cell average) {
        return maximum;
    }

}
