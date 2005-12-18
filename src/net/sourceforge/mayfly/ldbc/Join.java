package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

public abstract class Join extends ValueObject implements FromElement {

    public static Join fromJoinTree(Tree tree, TreeConverters converters) {
        Tree.Children children = tree.children();
        
        FromElement left = (FromElement) new Tree.Convert(converters).transform(children.element(0));
        Tree type = (Tree) children.element(1);
        FromElement right = (FromElement) new Tree.Convert(converters).transform(children.element(2));
        Where condition = (Where) new Tree.Convert(converters).transform(children.element(3));

        if (type.getType() == SQLTokenTypes.LITERAL_inner) {
            return new InnerJoin(left, right, condition);
        } else if (type.getType() == SQLTokenTypes.LITERAL_left) {
            return new LeftJoin(left, right, condition);
        } else {
            throw new MayflyException("Don't recognize token type for " + type.toStringTree());
        }
    }

    protected final FromElement right;
    protected final Where condition;
    protected final FromElement left;

    protected Join(FromElement left, FromElement right, Where condition) {
        this.left = left;
        this.right = right;
        this.condition = condition;
    }

    public Rows dummyRows(DataStore store, String currentSchema) {
        Rows dummyRows = (Rows) left.dummyRows(store, currentSchema).cartesianJoin(right.dummyRows(store, currentSchema));
        dummyRows.select(condition);
        return dummyRows;
    }

}
