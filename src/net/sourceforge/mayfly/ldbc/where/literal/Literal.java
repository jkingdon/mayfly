package net.sourceforge.mayfly.ldbc.where.literal;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.*;
import net.sourceforge.mayfly.ldbc.*;

public abstract class Literal extends Expression {

    public static Literal fromValue(Object value) {
        if (value instanceof Number) {
            Number number = (Number) value;
            return new MathematicalInt(number.intValue());
        }

        throw new RuntimeException("Don't know how to deal with value of class " + value.getClass());
    }

    public boolean matchesCell(Cell cell) {
        return cell.equals(Cell.fromContents(valueForCellContentComparison()));
    }

    public final Cell evaluate(Row row) {
        return valueAsCell();
    }

    public final Cell aggregate(Rows rows) {
        return valueAsCell();
    }
    
    abstract public Object valueForCellContentComparison();

    protected abstract Cell valueAsCell();

}
