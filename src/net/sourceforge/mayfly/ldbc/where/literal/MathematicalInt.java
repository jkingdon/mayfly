package net.sourceforge.mayfly.ldbc.where.literal;

import net.sourceforge.mayfly.datastore.*;

public class MathematicalInt extends Literal {

    private final int value;

    public MathematicalInt(int value) {
        this.value = value;
    }

    protected Cell valueAsCell() {
        return new LongCell(value);
    }

    public Object valueForCellContentComparison() {
        return new Long(value);
    }
    
    public String displayName() {
        return "" + value;
    }

}
