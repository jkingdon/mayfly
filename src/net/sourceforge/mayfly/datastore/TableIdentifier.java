package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;

public class TableIdentifier extends ValueObject {
    private String tableName;

    public TableIdentifier(String tableName) {
        this.tableName = tableName.toLowerCase();
    }
}
