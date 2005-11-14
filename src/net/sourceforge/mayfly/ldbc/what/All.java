package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

public class All extends WhatElement {

    public Columns columns() {
        throw new UnimplementedException("selecting everything (select *) not implemented");
    }

    public Tuples process(Tuples originalTuples, M aliasToTableName) {
        throw new RuntimeException();
    }
}
