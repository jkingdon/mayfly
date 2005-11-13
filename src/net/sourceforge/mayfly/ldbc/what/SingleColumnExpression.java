package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class SingleColumnExpression extends WhatElement implements Transformer {
    private Column column;

    private SingleColumnExpression(Column column) {
        this.column = column;
    }

    public SingleColumnExpression(String columnName) {
        this(null, columnName);
    }

    public SingleColumnExpression(String tableOrAlias, String columnName) {
        this(new Column(tableOrAlias, columnName));
    }

    public Columns columns() {
        return new Columns(new ImmutableList(column));
    }

    public Object transform(Object from) {
        Row row = (Row) from;
        return row.cell(column);
    }

}
