package net.sourceforge.mayfly.ldbc.rowmask;

import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.antlr.collections.*;

public class SingleColumnExpression extends RowMaskElement {
    private Column column;

    public static net.sourceforge.mayfly.ldbc.rowmask.SingleColumnExpression fromExpressionTree(Tree t) {
        AST expression = t.getFirstChild();

        Tree column = new Tree(expression.getFirstChild());
        return new SingleColumnExpression(Column.fromColumnTree(column));
    }

    public SingleColumnExpression(Column column) {
        this.column = column;
    }
}
