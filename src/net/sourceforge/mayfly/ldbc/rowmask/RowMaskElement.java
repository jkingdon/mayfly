package net.sourceforge.mayfly.ldbc.rowmask;

import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.parser.*;

abstract public class RowMaskElement extends ValueObject {

    public static RowMaskElement fromTree(Tree t) {
        if (t.getType() == SQLTokenTypes.TABLE_ASTERISK) {
            return WholeDimension.fromTree(t);
        }

        if (t.getType() == SQLTokenTypes.SELECT_ITEM) {
            return SingleColumnExpression.fromExpressionTree(t);
        }

        if (t.getType() == SQLTokenTypes.ASTERISK) {
            return new Everything();
        }

        throw new RuntimeException("row match class not found for " + t.getType());
    }


}
