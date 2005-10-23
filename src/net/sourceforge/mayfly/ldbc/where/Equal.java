package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;
import org.ldbc.antlr.collections.*;

import java.util.*;

public class Equal extends ValueObject implements Selector{
    public static Equal fromTree(Tree tree) {
        Iterator iter = tree.children().iterator();

        AST left = (AST) iter.next();
        Column column = Column.fromColumnTree(new Tree(left));

        AST right = (AST) iter.next();
        Literal quotedString = Literal.literalFromTree(new Tree(right));

        return new Equal(column, quotedString);
    }

    private Object leftside;
    private Object rightside;

    public Equal(Object leftside, Object rightside) {
        this.leftside = leftside;
        this.rightside = rightside;
    }

    public boolean evaluate(Object candidate) {
        Row r = (Row) candidate;

        return ((Literal)rightside).matchesCell(r.cell((Column) leftside));
    }
}
