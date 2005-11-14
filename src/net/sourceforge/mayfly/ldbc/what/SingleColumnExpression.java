package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.*;

import java.sql.*;

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

    public Tuples process(Tuples originalTuples) {
        try {
            Column column =
                originalTuples
                    .headers()
                        .thatAreColumns()
                            .columnFromName(this.column.columnName());

            return new Tuples(originalTuples.withHeader(column));
        } catch (SQLException e) {
            throw new MayflyException(e);
        }
    }

}
