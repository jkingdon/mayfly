package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

public class WhereTest extends TestCase {

    public void testWhere() throws Exception {
        assertEquals(
            new Where(
                new Eq(new SingleColumn("f", "name"), new QuotedString("'steve'"))
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
        Where where = new Where(new Eq(new SingleColumn("a"), new MathematicalInt(5)));
        Row fiveRow = new Row(new TupleElement(new Column("a"), new LongCell(5)));
        Row nullRow = new Row(new TupleElement(new Column("a"), NullCell.INSTANCE));
        
        assertTrue(where.evaluate(fiveRow));
        assertFalse(where.evaluate(nullRow));
    }


}
