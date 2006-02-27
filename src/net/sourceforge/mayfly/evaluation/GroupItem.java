package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.ldbc.what.*;

public class GroupItem {

    private final Expression expression;

    public GroupItem(Expression column) {
        this.expression = column;
    }

    public SingleColumn column() {
        return (SingleColumn) expression;
    }
    
    public Expression expression() {
        return expression;
    }

}
