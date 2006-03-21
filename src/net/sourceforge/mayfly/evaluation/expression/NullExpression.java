package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.ldbc.Rows;

public class NullExpression extends Expression {

    public Cell evaluate(Row row) {
        return NullCell.INSTANCE;
    }

    public Cell aggregate(Rows rows) {
        return NullCell.INSTANCE;
    }

    public boolean sameExpression(Expression other) {
        return other instanceof NullExpression;
    }

    public String displayName() {
        return "null";
    }

}
