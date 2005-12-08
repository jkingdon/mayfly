package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public class Concatenate extends WhatElement {

    private final WhatElement left;
    private final WhatElement right;

    public Concatenate(WhatElement left, WhatElement right) {
        this.left = left;
        this.right = right;
    }

    public Cell evaluate(Row row) {
        Cell leftCell = left.evaluate(row);
        Cell rightCell = right.evaluate(row);
        return new StringCell(leftCell.asString() + rightCell.asString());
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }

    public String firstColumn() {
        String firstInLeft = left.firstColumn();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstColumn();
    }

    public String firstAggregate() {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

}
