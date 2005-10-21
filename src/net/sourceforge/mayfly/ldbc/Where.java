package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;
import org.ldbc.antlr.collections.*;

import java.util.*;

public class Where extends ValueObject {

    private L expressions = new L();

    public Where add(Equal eq) {
        expressions.add(eq);
        return this;
    }


    public static Where fromConditionTree(Tree t) {

        //pretty baked.  that's ok at the moment.

        return
            new Where()
                .add(Where.Equal.fromTree(new Tree(t.getFirstChild())));
    }

    public static class Equal extends ValueObject {
        public static Equal fromTree(Tree tree) {
            Iterator iter = tree.children().iterator();

            AST left = (AST) iter.next();
            Column column = Column.fromColumnTree(new Tree(left));

            AST right = (AST) iter.next();
            Literal.QuotedString quotedString = Literal.QuotedString.fromTree(new Tree(right));

            return new Equal(column, quotedString);
        }

        private Object leftside;
        private Object rightside;

        public Equal(Object leftside, Object rightside) {
            this.leftside = leftside;
            this.rightside = rightside;
        }
    }
}
