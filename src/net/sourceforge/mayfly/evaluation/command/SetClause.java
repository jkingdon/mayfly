package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.evaluation.Expression;

public class SetClause {

    private final String column;
    private final Expression value;

    public SetClause(String column, Expression value) {
        this.column = column;
        this.value = value;
    }

    public String column() {
        return column;
    }

    public Expression value() {
        return value;
    }

}
