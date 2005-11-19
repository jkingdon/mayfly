package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

abstract public class RowExpression extends BooleanExpression {

    private Transformer leftSide;
    private Transformer rightSide;

    public RowExpression(Transformer leftSide, Transformer rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;

        return compare((Cell) leftSide.transform(row), (Cell) rightSide.transform(row));
    }

    abstract protected boolean compare(Cell leftSide, Cell rightSide);

    public int parameterCount() {
        return parameterCount(leftSide) + parameterCount(rightSide);
    }

    public void substitute(Iterator jdbcParameters) {
        leftSide = substitute(leftSide, jdbcParameters);
        rightSide = substitute(rightSide, jdbcParameters);
    }

}
