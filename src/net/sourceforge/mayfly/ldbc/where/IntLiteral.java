package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

public class IntLiteral extends Literal {

    private final int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    public static Literal fromTree(Tree tree) {
        return new IntLiteral(Integer.parseInt(tree.getFirstChild().getText()));
    }

    public boolean matchesCell(Cell cell) {
        return value == cell.asInt();
    }

}
