package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class SingleColumn extends WhatElement implements Transformer {
    private String tableOrAlias;
    private String columnName;

    public SingleColumn(String columnName) {
        this(null, columnName);
    }

    public SingleColumn(String tableOrAlias, String columnName) {
        this.tableOrAlias = tableOrAlias;
        this.columnName = columnName;
    }

    public Cell evaluate(Row row) {
        return row.cell(tableOrAlias, columnName);
    }

    public Object transform(Object from) {
        return evaluate((Row) from);
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        String tableName =
            aliasToTableName.containsKeyCaseInsensitive(tableOrAlias) ?
                (String)aliasToTableName.getCaseInsensitive(tableOrAlias) :
                tableOrAlias;

        Columns possibleColumns = originalTuple
                                        .headers()
                                            .thatAreColumns();
        Column column =
            tableName == null ?
                 possibleColumns.columnFromName(columnName) :
                 possibleColumns.columnMatching(tableName, columnName);

        return new Tuple(originalTuple.withHeader(column));
    }

}
