package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.util.*;

public class InsertTable extends ValueObject {

    private final String tableName;

    public InsertTable(String tableName) {
        this.tableName = tableName;
    }

    public String tableName() {
        return tableName;
    }

}
