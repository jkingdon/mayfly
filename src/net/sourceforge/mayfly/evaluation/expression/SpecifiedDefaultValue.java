package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.literal.CellExpression;

public class SpecifiedDefaultValue extends DefaultValue {

    private final Expression expression;

    public SpecifiedDefaultValue(Cell value) {
        this(new CellExpression(value));
        if (value instanceof NullCell) {
            throw new MayflyInternalException(
                "null should be using UnspecifiedDefaultValue");
        }
    }

    public SpecifiedDefaultValue(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Cell cell() {
        return expression.evaluate((ResultRow)null);
    }

    @Override
    public boolean isSpecified() {
        return true;
    }

    @Override
    public boolean sqlEquals(Cell cell) {
        return cell().sqlEquals(cell);
    }

    @Override
    public String asSql() {
        return expression.asSql();
    }

}
