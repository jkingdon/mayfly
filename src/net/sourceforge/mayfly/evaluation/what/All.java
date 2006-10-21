package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.evaluation.ResultRow;

public class All extends WhatElement {

    public Selected selected(ResultRow dummyRow) {
        return new Selected(dummyRow.expressions());
    }

    public String displayName() {
        return "*";
    }

}
