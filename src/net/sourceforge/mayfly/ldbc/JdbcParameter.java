package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class JdbcParameter extends WhatElement implements Transformer {
    
    public static final JdbcParameter INSTANCE = new JdbcParameter();

    private JdbcParameter() {
    }

    public Object transform(Object from) {
        throw new MayflyInternalException("should have substituted jdbc parameters by now");
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }

    public Cell evaluate(Row row) {
        throw new MayflyInternalException("should have substituted jdbc parameters by now");
    }
    
    public Cell aggregate(Rows rows) {
        throw new MayflyInternalException("should have substituted jdbc parameters by now");
    }

}
