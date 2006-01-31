package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.ldbc.what.WhatElement;

abstract public class RowExpression extends BooleanExpression {

    /** Could be {@link net.sourceforge.mayfly.evaluation.Expression}
     *  instead of {@link WhatElement} once we figure out how JdbcParameters
     *  get substituted.
     */
    private WhatElement leftSide;
    private WhatElement rightSide;

    public RowExpression(WhatElement leftSide, WhatElement rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;

        return compare(leftSide.evaluate(row), rightSide.evaluate(row));
    }

    abstract protected boolean compare(Cell leftSide, Cell rightSide);

    public String firstAggregate() {
        return Expression.firstAggregate(leftSide, rightSide);
    }

}
