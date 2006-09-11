package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.PositionalHeader;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Selected extends ValueObject implements Iterable {

    private List expressions;

    public Selected() {
        this(new ArrayList());
    }

    public Selected(List expressions) {
        super();
        this.expressions = expressions;
    }

    public Iterator iterator() {
        return expressions.iterator();
    }

    public Selected add(Expression expression) {
        expressions.add(expression);
        return this;
    }

    public Cell evaluate(int oneBasedColumn, ResultRow row) {
        int zeroBasedColumn = oneBasedColumn - 1;
        if (zeroBasedColumn < 0 || zeroBasedColumn >= expressions.size()) {
            throw new MayflyException("no column " + oneBasedColumn);
        }
        Expression element = (Expression) expressions.get(zeroBasedColumn);
        return row.findValue(zeroBasedColumn, element);
    }

    public Rows aggregate(Rows rows) {
        TupleBuilder builder = new TupleBuilder();
        for (int i = 0; i < expressions.size(); ++i) {
            Expression element = (Expression) expressions.get(i);
            builder.append(new PositionalHeader(i), element.aggregate(rows));
        }
        Row resultRow = new Row(builder);
        return new Rows(resultRow);
    }

    public int size() {
        return expressions.size();
    }

    public Expression element(int index) {
        return (Expression) expressions.get(index);
    }

}
