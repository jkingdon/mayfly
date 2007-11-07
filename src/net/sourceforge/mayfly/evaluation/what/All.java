package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.evaluation.ResultRow;

public class All extends WhatElement {

    @Override
    public Selected selected(ResultRow dummyRow) {
        return new Selected(dummyRow.expressions());
    }

    @Override
    public String displayName() {
        return "*";
    }

}
