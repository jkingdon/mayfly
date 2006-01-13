package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

abstract public class BinaryOperator extends Expression {

    private final WhatElement left;
    private final WhatElement right;

    public BinaryOperator(WhatElement left, WhatElement right) {
        this.left = left;
        this.right = right;
    }

    public Cell evaluate(Row row) {
        Cell leftCell = left.evaluate(row);
        Cell rightCell = right.evaluate(row);
        return combine(leftCell, rightCell);
    }

    public Cell aggregate(Rows rows) {
        Cell leftCell = left.aggregate(rows);
        Cell rightCell = right.aggregate(rows);
        return combine(leftCell, rightCell);
    }
    
    public Cell findValue(int zeroBasedColumn, Row row) {
        if (firstAggregate() != null) {
            return row.byPosition(zeroBasedColumn);
        }
        else {
            return evaluate(row);
        }
    }
    
    abstract protected Cell combine(Cell left, Cell right);
    
    public String firstColumn() {
        String firstInLeft = left.firstColumn();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstColumn();
    }

    public String firstAggregate() {
        return WhatElement.firstAggregate(left, right);
    }

    public String displayName() {
        // Hard to get precedence and associativity right.
        // And we probably want to show the users's whitespace anyway(?).
        return "expression";
    }
    
}
