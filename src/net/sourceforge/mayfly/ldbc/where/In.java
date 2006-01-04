package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class In extends BooleanExpression {

    private Transformer leftSide;
	private List expressions;

	public static In fromInTree(Tree inTree, TreeConverters converters) {
        L converted = inTree.children().convertUsing(converters);

        Transformer leftSide = (Transformer) converted.get(0);
        List list = converted.subList(1);
        return new In(leftSide, list);
    }

    public In(Transformer leftSide, List expressions) {
		this.leftSide = leftSide;
		this.expressions = expressions;
    }

	public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;
        Cell cell = (Cell) leftSide.transform(row);

        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
			Literal element = (Literal) iter.next();
			if (element.matchesCell(cell)) {
                return true;
			}
		}
		return false;
	}

    public int parameterCount() {
        int listCount = 0;
        for (int i = 0; i < expressions.size(); ++i) {
            listCount += parameterCount((Transformer) expressions.get(i));
        }
        return parameterCount(leftSide) + listCount;
    }

    public void substitute(Iterator jdbcParameters) {
        leftSide = substitute(leftSide, jdbcParameters);
        for (int i = 0; i < expressions.size(); ++i) {
            expressions.set(i, substitute((Transformer) expressions.get(i), jdbcParameters));
        }
    }

}
