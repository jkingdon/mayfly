package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.parser.*;

public class IsNullTest extends TestCase {

    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name is null");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Tree isNullTree = new Tree(whereClause.getFirstChild());

        assertEquals(
                new IsNull(new SingleColumn("name")),
                IsNull.fromIsNullTree(isNullTree, TreeConverters.forWhereTree())
        );
    }

    public void testEvaluate() throws Exception {
        Row nullRow = new Row(
            new TupleBuilder()
                .appendColumnCell("colA", NullCell.INSTANCE)
        );

        assertTrue(new IsNull(new SingleColumn("colA")).evaluate(nullRow));

        Row nonNullRow = new Row(
            new TupleBuilder()
                .appendColumnCell("colA", new StringCell("foo"))
        );

        assertFalse(new IsNull(new SingleColumn("colA")).evaluate(nonNullRow));
    }

}
