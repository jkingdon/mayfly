package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

public class WhereTest extends TestCase {

    public void testEquals() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where f.name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);

        assertEquals(
            new Eq(new SingleColumn("f", "name"), new QuotedString("'steve'")),
            Eq.fromEqualTree(new Tree(whereClause.getFirstChild()), TreeConverters.forWhereTree())
        );

    }

    public void testWhere() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where f.name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);

        assertEquals(
            new Where(
                new Eq(new SingleColumn("f", "name"), new QuotedString("'steve'"))
            ),
            Where.fromConditionTree(whereClause)
        );
    }



    public void testSelect() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name='steve'");
        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Where where = Where.fromConditionTree(whereClause);

        Row row1 = new Row(new TupleElement(new Column("name"), new StringCell("steve")));
        Row row2 = new Row(new TupleElement(new Column("name"), new StringCell("bob")));

        assertTrue(where.evaluate(row1));
        assertFalse(where.evaluate(row2));
    }
    
    public void testNull() throws Exception {
        Where where = new Where(new Eq(new SingleColumn("a"), new MathematicalInt(5)));
        Row fiveRow = new Row(new TupleElement(new Column("a"), new LongCell(5)));
        Row nullRow = new Row(new TupleElement(new Column("a"), NullCell.INSTANCE));
        
        assertTrue(where.evaluate(fiveRow));
        assertFalse(where.evaluate(nullRow));
    }


}
