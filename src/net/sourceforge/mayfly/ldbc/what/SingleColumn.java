package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.parser.Location;

public class SingleColumn extends Expression {
    private final String originalTableOrAlias;
    private final String tableOrAlias;
    private final String columnName;

    public SingleColumn(String columnName) {
        this(null, columnName);
    }

    public SingleColumn(String columnName, Location location) {
        this(null, columnName, location);
    }

    public SingleColumn(String tableOrAlias, String columnName) {
        this(tableOrAlias, columnName, Location.UNKNOWN);
    }

    public SingleColumn(String tableOrAlias, String columnName, Location location) {
        super(location);
        this.tableOrAlias = tableOrAlias;
        this.originalTableOrAlias = tableOrAlias;
        this.columnName = columnName;
    }

    private SingleColumn(String tableOrAlias, String originalTableOrAlias, String columnName) {
        this.tableOrAlias = tableOrAlias;
        this.originalTableOrAlias = originalTableOrAlias;
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
    
    public boolean matches(String target) {
        if (target.indexOf('.') != -1) {
            throw new MayflyException("column name " + target + " should not contain a period");
        }

        return this.columnName.equalsIgnoreCase(target);
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
    
    public Expression resolveAndReturn(Row row) {
        if (tableOrAlias == null) {
            Column column = lookup(row);
            if (column.tableOrAlias() == null) {
                throw new NullPointerException();
            }
            return new SingleColumn(column.tableOrAlias(), originalTableOrAlias, columnName);
        }
        else {
            return this;
        }
    }
    
    public String tableOrAlias() {
        return tableOrAlias;
    }

    public String columnName() {
        return columnName;
    }

}
