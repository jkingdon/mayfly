package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class JdbcParameter extends WhatElement implements Transformer {
    
    public static final JdbcParameter INSTANCE = new JdbcParameter();

    private JdbcParameter() {
    }

    public Columns columns() {
        throw new MayflyException("internal error: should have substituted jdbc parameters by now");
    }

    public Object transform(Object from) {
        throw new MayflyException("internal error: should have substituted jdbc parameters by now");
    }

    public Tuples process(Tuples originalTuples) {
        throw new RuntimeException();
    }
}
