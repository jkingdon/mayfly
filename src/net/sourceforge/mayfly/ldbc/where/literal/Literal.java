package net.sourceforge.mayfly.ldbc.where.literal;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public abstract class Literal extends ValueObject implements Transformer {

    public abstract boolean matchesCell(Cell cell);

    public Object transform(Object from) {
        return new Cell(valueForCellContentComparison());
    }

    abstract public Object valueForCellContentComparison();

}
