package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.*;
import net.sourceforge.mayfly.ldbc.*;

public class SingleColumn extends Expression {
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
    
    public Column lookup(Row row) {
        return row.findColumn(tableOrAlias, columnName);
    }

    public Cell aggregate(Rows rows) {
        throw new MayflyInternalException("shouldn't combine aggregate and column expressions");
    }

    public String firstColumn() {
        return displayName();
    }

    public String displayName() {
        return Column.displayName(tableOrAlias, columnName);
    }

    public boolean matches(Column column) {
        return column.matches(tableOrAlias, columnName);
    }
    
    public boolean sameExpression(Expression other) {
        if (other instanceof SingleColumn) {
            SingleColumn column = (SingleColumn) other;
            return possiblyNullEquals(tableOrAlias, column.tableOrAlias) &&
                columnName.equalsIgnoreCase(column.columnName);
        }
        else {
            return false;
        }
    }

    public static boolean possiblyNullEquals(String one, String two) {
        if (one == null) {
            return two == null;
        }
        else {
            return one.equalsIgnoreCase(two);
        }
    }

}
