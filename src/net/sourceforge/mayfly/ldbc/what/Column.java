package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;
import org.ldbc.antlr.collections.*;

public class Column implements Transformer {
    public static Column fromColumnTree(Tree column) {
        AST firstIdentifier = column.getFirstChild();
        AST secondIdentifier = firstIdentifier.getNextSibling();
        
        if (secondIdentifier == null) {
            String columnName = firstIdentifier.getText();
            return new Column(columnName);
        } else {
            String dimensionIdentifier = firstIdentifier.getText();
            String columnName = secondIdentifier.getText();
            return new Column(dimensionIdentifier, columnName);
        }
    }


    private final String table;
    private final String columnName;

    public Column(String table, String columnName) {
        this.table = table;
        this.columnName = columnName;
    }

    public Column(String column) {
        this(null, column);
    }
    
    public String columnName() {
        return columnName;
    }

    public boolean matchesName(String otherName) {
        return columnName.equalsIgnoreCase(otherName);
    }

    public boolean equals(Object other) {
        // TODO: This is broken.  If other isn't a column, should be a
        // false return, not ClassCastException
        String otherName = ((Column)other).columnName;
        return matchesName(otherName);
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return columnName;
    }

    public String table() {
        return table;
    }

    public Object transform(Object from) {
        Row row = (Row) from;
        return row.cell(this);
    }

}
