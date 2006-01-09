package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class Divide extends BinaryOperator {

    public Divide(WhatElement left, WhatElement right) {
        super(left, right);
    }

    protected Cell combine(Cell left, Cell right) {
        return new LongCell(left.asLong() / right.asLong());
    }

}
