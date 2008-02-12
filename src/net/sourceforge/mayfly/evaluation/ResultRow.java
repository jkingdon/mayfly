package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @internal
 * Mapping from Expression to Cell.
 * 
 * Unlike a {@link Row}, here we have an ordering.  In some contexts
 * (for example, evaluating an expression), the order doesn't matter,
 * and in fact might be constructed haphazardly.
 * 
 * However, when copying data which we'll return in a select, the
 * order here needs to
 * match the order of columns in a table (for "select foo.*"),
 * and even has some user-visible meaning between tables (for "select *"),
 * although in the latter case I'm not sure what the meaning
 * needs to be.
 */
public class ResultRow {
    
    private final ImmutableList<Element> elements;

    public ResultRow(Row row, String table) {
        this(row, table, new Options());
    }

    public ResultRow(Row row, String table, Options options) {
        this.elements = fromRow(row, table, options);
    }

    public ResultRow() {
        this(new ImmutableList());
    }
    
    private ResultRow(ImmutableList elements) {
        this.elements = elements;
    }

    private static ImmutableList<Element> fromRow(Row row, String table,
        Options options) {
        List result = new ArrayList();
        for (Iterator iter = row.columnNames(); iter.hasNext();) {
            CaseInsensitiveString column = (CaseInsensitiveString) iter.next();
            result.add(new Element(
                new SingleColumn(table, column.getString(), options), 
                row.cell(column)));
        }
        return new ImmutableList(result);
    }

    public int size() {
        return elements.size();
    }

    public Element element(int index) {
        return elements.get(index);
    }
    
    public Cell cell(int index) {
        return element(index).value;
    }
    
    public Expression expression(int index) {
        return element(index).expression;
    }

    public Expression findColumn(String columnName) {
        return findColumn(null, columnName);
    }

    public SingleColumn findColumn(String tableOrAlias, String columnName) {
        return findColumn(tableOrAlias, columnName, Location.UNKNOWN);
    }

    public SingleColumn findColumn(String tableOrAlias, String columnName,
        Location location) {
        SingleColumn found = null;
        for (Element element : elements) {
            if (element.expression instanceof SingleColumn) {
                SingleColumn candidate = (SingleColumn) element.expression;
                if (candidate.matches(tableOrAlias, columnName)) {
                    if (found != null) {
                        throw new MayflyException(
                            "ambiguous column " + columnName,
                            location);
                    }
                    else {
                        found = candidate;
                    }
                }
            }
        }
        if (found == null) {
            throw new NoColumn(tableOrAlias, columnName, location);
        } else {
            return found;
        }
    }
    
    public Cell findValue(Expression target) {
        Cell result = findValueOrNull(target);
        if (result == null) {
            throw new MayflyInternalException(
                "Where did expression " + target.displayName() + " come from?");
        }
        else {
            return result;
        }
    }

    public Cell findValueOrNull(Expression target) {
        Expression resolved = target.resolve(this);
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            if (resolved.matches(element.expression)) {
                return element.value;
            }
        }
        return null;
    }

    public Cell findOrEvaluate(Expression expression) {
        Cell result = findValueOrNull(expression);
        if (result != null) {
            return result;
        }
        else {
            return expression.evaluate(this);
        }
    }
    
    public ResultRow withColumn(String tableOrAlias, String columnName, Cell cell) {
        return with(new SingleColumn(tableOrAlias, columnName), cell);
    }

    public ResultRow withColumn(String tableOrAlias, String column, String value) {
        return withColumn(tableOrAlias, column, new StringCell(value));
    }

    public ResultRow with(Expression expression, Cell value) {
        return new ResultRow(elements.with(new Element(expression, value)));
    }
    
    public String debugString() {
        StringBuilder out = new StringBuilder();
        out.append("Result Row:\n");
        for (Element element : elements) {
            out.append("  ");
            out.append(element.expression.displayName());
            out.append(" = ");
            out.append(element.value.displayName());
            out.append("\n");
        }
        return out.toString();
    }
    
    public static class Element {
        public final Expression expression;
        public final Cell value;

        public Element(Expression expression, Cell value) {
            /* This is probably where we want to go eventually.
               But we need to work through things like how
               we convert a Row to a ResultRow (do we even
               need to do that, outside applyAlias, beyond
               the transition phase)?  There are also issues
               with group-by keys.
            if (expression instanceof SingleColumn) {
                // might also want to recurse to make sure all
                // sub-expressions are resolved.  But this will
                // do for now.
                ((SingleColumn)expression).assertIsResolved();
            }
            */
            this.expression = expression;
            this.value = value;
        }

        public SingleColumn column() {
            return (SingleColumn)expression;
        }
        
    }

    /**
     * Return a new row which has all the columns from two input
     * rows.
     */
    public ResultRow combine(ResultRow right) {
        Set leftTableNames = new HashSet();

        List result = new ArrayList();
        Iterator leftIterator = elements.iterator();
        while (leftIterator.hasNext()) {
            Element element = (Element) leftIterator.next();
            leftTableNames.add(new CaseInsensitiveString(
                element.column().tableOrAlias()));
            result.add(element);
        }

        Iterator rightIterator = right.elements.iterator();
        while (rightIterator.hasNext()) {
            Element element = (Element) rightIterator.next();
            String tableOrAlias = element.column().tableOrAlias();
            if (leftTableNames.contains(new CaseInsensitiveString(tableOrAlias))) {
                throw new MayflyException(
                    "duplicate table name or alias " + tableOrAlias);
            }
            result.add(element);
        }

        return new ResultRow(new ImmutableList(result));
    }

    public ImmutableList expressions() {
        List result = new ArrayList();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            result.add(element.expression);
        }
        return new ImmutableList(result);
    }

    public ImmutableList expressionsForTable(String aliasOrTable) {
        List found = new ArrayList();
        for (Element element : elements) {
            if (element.expression instanceof SingleColumn) {
                SingleColumn column = (SingleColumn) element.expression;
                if (column.matchesAliasOrTable(aliasOrTable)) {
                    found.add(column);
                }
            }
        }
        return new ImmutableList(found);
    }

}
