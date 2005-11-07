package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.Tree.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

import org.ldbc.parser.*;

import java.util.*;

public class Insert extends ValueObject {

    /*private*/ final InsertTable table;
    /*private*/ final List columns;
    /*private*/ final List values;

    public Insert(InsertTable table, List columns, List values) {
        this.table = table;
        this.columns = columns;
        this.values = values;
    }

    public static Insert fromTree(Tree tree) {
        Children children = tree.children();
        Tree tableIdentifier = (Tree) children.element(0);
        Tree columnList = (Tree) children.element(1);
        Tree values = (Tree) children.element(2);
        
        return new Insert(
            new InsertTable(tableIdentifier.getText()),
            fromColumnList(columnList),
            fromValues(values)
        );
    }

    private static List fromValues(Tree values) {
        List result = new L();
        Iterator iter = values.children().iterator();
        while (iter.hasNext()) {
            Tree value = (Tree) iter.next();
            result.add(convertValue(value));
        }
        return result;
    }

    private static Object convertValue(Tree value) {
        if (value.getType() == SQLTokenTypes.DECIMAL_VALUE) {
            return MathematicalInt.fromDecimalValueTree(value).valueForCellContentComparison();
        } else {
            throw new UnimplementedException("Don't know how to convert " + value);
        }
    }

    private static List fromColumnList(Tree columnList) {
        List result = new L();
        Iterator iter = columnList.children().iterator();
        while (iter.hasNext()) {
            Tree column = (Tree) iter.next();
            result.add(column.getText());
        }
        return result;
    }

}
