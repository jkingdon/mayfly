package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;

abstract public class RowExpression extends ValueObject implements Selector {

    private Transformer leftSide;
    private Transformer rightSide;

    public RowExpression(Transformer leftSide, Transformer rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object candidate) {
        Row row = (Row) candidate;

        return compare((Cell)leftSide.transform(row), (Cell) rightSide.transform(row));
    }

    abstract protected boolean compare(Cell leftSide, Cell rightSide);
}
