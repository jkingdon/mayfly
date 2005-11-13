package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class In extends BooleanExpression {

    private Transformer leftSide;
	private List list;

	public static In fromInTree(Tree inTree, TreeConverters converters) {
        L converted = inTree.children().convertUsing(converters);

        Transformer leftSide = (Transformer) converted.get(0);
        List list = converted.subList(1);
        return new In(leftSide, list);
    }

    public In(Transformer leftSide, List list) {
		this.leftSide = leftSide;
		this.list = list;
    }

	public boolean evaluate(Object candidate) {
        Row row = (Row) candidate;
        Cell cell = (Cell) leftSide.transform(row);

        for (Iterator iter = list.iterator(); iter.hasNext();) {
			Literal element = (Literal) iter.next();
			if (element.matchesCell(cell)) {
                return true;
			}
		}
		return false;
	}

    public int parameterCount() {
        int listCount = 0;
        for (int i = 0; i < list.size(); ++i) {
            listCount += parameterCount((Transformer) list.get(i));
        }
        return parameterCount(leftSide) + listCount;
    }

    public void substitute(Iterator jdbcParameters) {
        leftSide = substitute(leftSide, jdbcParameters);
        for (int i = 0; i < list.size(); ++i) {
            list.set(i, substitute((Transformer) list.get(i), jdbcParameters));
        }
    }

}
