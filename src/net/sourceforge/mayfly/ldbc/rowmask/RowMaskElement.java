package net.sourceforge.mayfly.ldbc.rowmask;

import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.parser.*;

abstract public class RowMaskElement extends ValueObject {

    public static RowMaskElement fromTree(Tree t) {
        if (t.getType() == SQLTokenTypes.TABLE_ASTERISK) {
            return AllColumnsFromTable.fromTree(t);
        }

        if (t.getType() == SQLTokenTypes.SELECT_ITEM) {
            return SingleColumnExpression.fromExpressionTree(t);
        }

        if (t.getType() == SQLTokenTypes.ASTERISK) {
            return new All();
        }

        throw new RuntimeException("row match class not found for " + t.getType());
    }


}
