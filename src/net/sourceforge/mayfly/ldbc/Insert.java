package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

import org.ldbc.parser.*;

import java.sql.*;
import java.util.*;

public class Insert extends Command {

    private final InsertTable table;
    private final List columns;
    private final List values;

    public Insert(InsertTable table, List columns, List values) {
        this.table = table;
        this.columns = columns;
        this.values = values;
    }

    public static Insert insertFromTree(Tree tree) {
        Tree.Children children = tree.children();
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
        switch (value.getType()) {
        case SQLTokenTypes.DECIMAL_VALUE:
            return MathematicalInt.fromDecimalValueTree(value).valueForCellContentComparison();

        case SQLTokenTypes.PARAMETER:
            return JdbcParameter.INSTANCE;

        case SQLTokenTypes.QUOTED_STRING:
            return QuotedString.fromQuotedStringTree(value).valueForCellContentComparison();

        default:
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

    public List columns() {
        return columns;
    }

    public String table() {
        return table.tableName();
    }

    public List values() {
        return values;
    }

    public void substitute(Collection jdbcParameters) {
        substitute(jdbcParameters.iterator());
    }

    private void substitute(Iterator jdbcParameters) {
        for (int i = 0; i < values.size(); ++i) {
            if (values.get(i) instanceof JdbcParameter) {
                values.set(i, jdbcParameters.next());
            }
        }
    }

    public DataStore update(DataStore store) throws SQLException {
        return store.addRow(table(), columns(), values());
    }

    public int rowsAffected() {
        return 1;
    }
    
    public int parameterCount() {
        int count = 0;
        for (int i = 0; i < values().size(); ++i) {
            if (values.get(i) instanceof JdbcParameter) {
                ++count;
            }
        }
        return count;
    }

}
