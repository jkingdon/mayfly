package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;

public class SingleColumn extends Expression {
    private final String originalTableOrAlias;
    private final String tableOrAlias;
    private final String columnName;
    public final Options options;

    public SingleColumn(String columnName) {
        this(null, columnName);
    }

    public SingleColumn(String columnName, Location location, Options options) {
        this(null, columnName, location, options);
    }

    public SingleColumn(String tableOrAlias, String columnName) {
        this(tableOrAlias, columnName, new Options());
    }

    public SingleColumn(String tableOrAlias, String columnName, Options options) {
        this(tableOrAlias, columnName, Location.UNKNOWN, options);
    }

    public SingleColumn(String tableOrAlias, String columnName, 
        Location location, Options options) {
        this(tableOrAlias, tableOrAlias, columnName, location, options);
    }

    private SingleColumn(String tableOrAlias, String originalTableOrAlias, 
        String columnName, Location location, Options options) {
        super(location);
        this.tableOrAlias = tableOrAlias;
        this.originalTableOrAlias = originalTableOrAlias;
        this.columnName = columnName;
        this.options = options;
    }

    @Override
    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        return evaluator.lookup(row, tableOrAlias, columnName, location);
    }

    @Override
    public Cell evaluate(ResultRow row) {
        Expression found = row.findColumn(tableOrAlias, columnName, location);
        return row.findValue(found);
    }

    public SingleColumn lookup(ResultRow row) {
        return row.findColumn(tableOrAlias, columnName, location);
    }

    @Override
    public Cell aggregate(ResultRows rows) {
        throw new MayflyInternalException(
            "shouldn't combine aggregate and column expressions");
    }

    @Override
    public String firstColumn() {
        return displayName();
    }

    @Override
    public String displayName() {
        return Column.displayName(originalTableOrAlias, columnName);
    }

    @Override
    public String debugName() {
        return Column.displayName(tableOrAlias, columnName);
    }

    @Override
    public boolean matches(String target) {
        return matches(null, target);
    }

    public boolean matches(String tableOrAlias, String target) {
        if (target.indexOf('.') != -1) {
            throw new MayflyException("column name " + target + " should not contain a period");
        }

        if (tableOrAlias != null && !matchesAliasOrTable(tableOrAlias)) {
            return false;
        }
        return this.columnName.equalsIgnoreCase(target);
    }

    public boolean matchesAliasOrTable(String tableOrAlias) {
        return options.tableNamesEqual(tableOrAlias, this.tableOrAlias);
    }

    @Override
    public boolean sameExpression(Expression other) {
        if (other instanceof SingleColumn) {
            SingleColumn column = (SingleColumn) other;
            return possiblyNullTablesEqual(tableOrAlias, column.tableOrAlias) &&
                columnName.equalsIgnoreCase(column.columnName);
        }
        else {
            return false;
        }
    }

    boolean possiblyNullTablesEqual(String one, String two) {
        if (one == null) {
            return two == null;
        }
        else {
            return options.tableNamesEqual(one, two);
        }
    }
    
    @Override
    public Expression resolve(ResultRow row) {
        if (tableOrAlias == null) {
            SingleColumn column = lookup(row);
//            if (column.tableOrAlias() == null) {
//                throw new NullPointerException();
//            }
            return new SingleColumn(column.tableOrAlias(), 
                originalTableOrAlias, columnName, location, options);
        }
        else {
            return this;
        }
    }
    
    @Override
    public void check(ResultRow row) {
        lookup(row);
    }
    
    public String tableOrAlias() {
        return tableOrAlias;
    }

    public String columnName() {
        return columnName;
    }
    
    public void assertIsResolved() {
        if (tableOrAlias == null) {
            throw new MayflyInternalException(
                "column " + displayName() + 
                " should have been resolved to a table/alias");
        }
    }
    
}
