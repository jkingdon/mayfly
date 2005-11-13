package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.*;

public class StringStartsWith extends BooleanExpression {
    private String prefix;

    public StringStartsWith(String prefix) {
        this.prefix = prefix;
    }

    public boolean evaluate(Object candidate) {
        return candidate.toString().startsWith(prefix);
    }

    public int parameterCount() {
        throw new UnimplementedException();
    }

}
