package net.sourceforge.mayfly.ldbc.where.literal;

import org.ldbc.parser.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public abstract class Literal extends ValueObject {

    public static Literal literalFromTree(Tree tree) {
        switch (tree.getType()) {
        case SQLTokenTypes.QUOTED_STRING:
            return QuotedString.fromQuotedStringTree(tree);
        case SQLTokenTypes.DECIMAL_VALUE:
            return Int.fromDecimalValueTree(tree);
        default:
            throw new RuntimeException("unexpected SQL Token type " + tree.getType());
        }
    }

    public abstract boolean matchesCell(Cell cell);

}
