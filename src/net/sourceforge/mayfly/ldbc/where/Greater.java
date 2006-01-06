package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class Greater extends RowExpression {

    public Greater(WhatElement leftSide, WhatElement rightSide) {
        super(leftSide, rightSide);
    }

    protected boolean compare(Cell leftSide, Cell rightSide) {
        return leftSide.asLong() > rightSide.asLong();
    }

}
