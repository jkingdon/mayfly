package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

import java.util.*;

public class IsNull extends BooleanExpression {

    private final WhatElement expression;

    public IsNull(WhatElement expression) {
        this.expression = expression;
        
    }

    public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;
        Cell cell = expression.evaluate(row);
        return cell instanceof NullCell;
    }

    public int parameterCount() {
        return 0;
    }

    public void substitute(Iterator jdbcParameters) {
    }
    
    public String firstAggregate() {
        return expression.firstAggregate();
    }

}
