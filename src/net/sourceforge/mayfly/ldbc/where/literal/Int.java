package net.sourceforge.mayfly.ldbc.where.literal;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

public class Int extends Literal {

    private final int value;

    public Int(int value) {
        this.value = value;
    }

    public static Literal fromDecimalValueTree(Tree tree) {
        return new Int(Integer.parseInt(tree.getFirstChild().getText()));
    }

    public boolean matchesCell(Cell cell) {
        return value == cell.asInt();
    }

}
