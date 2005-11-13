package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

abstract public class RowExpression extends BooleanExpression {

    private Transformer leftSide;
    private Transformer rightSide;

    public RowExpression(Transformer leftSide, Transformer rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object candidate) {
        Row row = (Row) candidate;

        return compare((Cell) leftSide.transform(row), (Cell) rightSide.transform(row));
    }

    abstract protected boolean compare(Cell leftSide, Cell rightSide);

    public int parameterCount() {
        return parameterCount(leftSide) + parameterCount(rightSide);
    }

}
