package net.sourceforge.mayfly.ldbc.where;

import java.util.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class IsNull extends BooleanExpression {

    public static IsNull fromIsNullTree(Tree tree, TreeConverters converters) {
        L converted = tree.children().convertUsing(converters);
        return new IsNull((Transformer) converted.get(0));
    }
    
    public static BooleanExpression fromIsNotNullTree(Tree tree, TreeConverters converters) {
        L converted = tree.children().convertUsing(converters);
        return new Not(new IsNull((Transformer) converted.get(0)));
    }

    private final Transformer expression;

    public IsNull(Transformer expression) {
        this.expression = expression;
        
    }

    public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;
        Cell cell = (Cell) expression.transform(row);
        return cell instanceof NullCell;
    }

    public int parameterCount() {
        return 0;
    }

    public void substitute(Iterator jdbcParameters) {
    }

}
