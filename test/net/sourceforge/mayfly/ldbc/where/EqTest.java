package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import org.ldbc.parser.*;

public class EqTest extends TestCase {

    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Tree equalTree = new Tree(whereClause.getFirstChild());

        assertEquals(
                new Eq(new SingleColumnExpression("name"), new QuotedString("'steve'")),
                Eq.fromEqualTree(equalTree, TreeConverters.forWhereTree())
        );
    }

    public void testColumnAndQuotedString() throws Exception {
        Row row = new Row(
            new Tuples()
                .appendColumnCellTuple("colA", "1")
        );

        assertTrue(new Eq(new SingleColumnExpression("colA"), new QuotedString("'1'")).evaluate(row));
        assertFalse(new Eq(new SingleColumnExpression("colA"), new QuotedString("'2'")).evaluate(row));
    }
}