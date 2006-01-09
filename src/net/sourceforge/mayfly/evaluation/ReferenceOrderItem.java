package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class ReferenceOrderItem extends OrderItem {

    private final int reference;

    public ReferenceOrderItem(int reference, boolean ascending) {
        super(ascending);
        this.reference = reference;
    }

    protected int compareAscending(final What what, Row first, Row second) {
        WhatElement whatElement = (WhatElement) what.element(reference - 1);
        if (whatElement instanceof SingleColumn) {
            return ColumnOrderItem.compare(first, second, (SingleColumn) whatElement);
        }
        else {
            throw new MayflyException("ORDER BY " + reference + " refers to an expression not a column");
        }
    }

    public void check(Row dummyRow) {
    }

}
