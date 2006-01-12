package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class Concatenate extends BinaryOperator {

    public Concatenate(WhatElement left, WhatElement right) {
        super(left, right);
    }

    protected Cell combine(Cell leftCell, Cell rightCell) {
        return new StringCell(leftCell.asString() + rightCell.asString());
    }
    
}
