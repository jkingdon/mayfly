package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.ldbc.Rows;

public class SingleColumn extends Expression {
    private final String originalTableOrAlias;
    private String tableOrAlias;
    private final String columnName;

    public SingleColumn(String columnName) {
        this(null, columnName);
    }

    public SingleColumn(String tableOrAlias, String columnName) {
        this.tableOrAlias = tableOrAlias;
        this.originalTableOrAlias = tableOrAlias;
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
        return Column.displayName(originalTableOrAlias, columnName);
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
    
    public void resolve(Row row) {
        if (tableOrAlias == null) {
            Column column = lookup(row);
            if (column.tableOrAlias() == null) {
                throw new NullPointerException();
            }
            tableOrAlias = column.tableOrAlias();
        }
    }

    public String tableOrAlias() {
        return tableOrAlias;
    }

    public String columnName() {
        return columnName;
    }

}
