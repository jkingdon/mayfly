package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class JdbcParameter extends WhatElement {
    
    public static final JdbcParameter INSTANCE = new JdbcParameter();

    private JdbcParameter() {
    }

    public Cell evaluate(Row row) {
        throw new MayflyInternalException("should have substituted jdbc parameters by now");
    }
    
    public Cell aggregate(Rows rows) {
        throw new MayflyInternalException("should have substituted jdbc parameters by now");
    }
    
    public String displayName() {
        return "?";
    }

}
