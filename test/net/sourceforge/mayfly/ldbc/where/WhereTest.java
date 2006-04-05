package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.parser.Parser;

public class WhereTest extends TestCase {

    public void testWhere() throws Exception {
        assertEquals(
            new Where(
                new Equal(new SingleColumn("f", "name"), new QuotedString("'steve'"))
            ),
            new Parser("f.name='steve'").parseWhere()
        );
    }

    public void testSelect() throws Exception {
        Where where = new Parser("name='steve'").parseWhere();

        Row row1 = new Row(new TupleElement(new Column("name"), new StringCell("steve")));
        Row row2 = new Row(new TupleElement(new Column("name"), new StringCell("bob")));

        assertTrue(where.evaluate(row1));
        assertFalse(where.evaluate(row2));
    }
    
    public void testNull() throws Exception {
        Where where = new Where(new Equal(new SingleColumn("a"), new IntegerLiteral(5)));
        Row fiveRow = new Row(new TupleElement(new Column("a"), new LongCell(5)));
        Row nullRow = new Row(new TupleElement(new Column("a"), NullCell.INSTANCE));
        
        assertTrue(where.evaluate(fiveRow));
        assertFalse(where.evaluate(nullRow));
    }


}
