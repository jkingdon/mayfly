package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.evaluation.what.WhatElement;

public class ReferenceOrderItem extends OrderItem {

    private final int reference;

    public ReferenceOrderItem(int reference, boolean ascending) {
        super(ascending);
        this.reference = reference;
    }

    protected int compareAscending(
        What what, ResultRow first, ResultRow second) {
        int zeroBasedColumn = reference - 1;

        int size = what.size();
        if (zeroBasedColumn < 0 || zeroBasedColumn >= size) {
            throw new MayflyException(
                "ORDER BY " + reference + " must be in range 1 to " + size);
        }

        WhatElement whatElement = what.element(zeroBasedColumn);
        if (whatElement instanceof SingleColumn) {
            return ColumnOrderItem.compare(first, second, (SingleColumn) whatElement);
        }
        else {
            throw new MayflyException(
                "ORDER BY " + reference + 
                " refers to an expression not a column");
        }
    }

    public void check(ResultRow afterGroupByAndDistinct, ResultRow afterJoins) {
    }

}
