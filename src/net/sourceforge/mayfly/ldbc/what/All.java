package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;

public class All extends WhatElement {

    public String columnName() {
        throw new UnimplementedException("selecting everything (select *) not implemented");
    }

}
