package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.what.*;

public class NotEqual {

    public static BooleanExpression construct(WhatElement left, WhatElement right) {
        // Not sure we can handle nulls right by doing it this way.
        // Certainly we aren't dealing with nulls correctly yet.
        return new Not(new Equal(left, right));
    }

}
