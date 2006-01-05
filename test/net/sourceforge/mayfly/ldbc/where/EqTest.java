package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

public class EqTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
                new Eq(new SingleColumn("name"), new QuotedString("'steve'")),
                new Parser("name='steve'").parseCondition()
        );
    }

    public void testColumnAndQuotedString() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
        );

        assertTrue(new Eq(new SingleColumn("colA"), new QuotedString("'1'")).evaluate(row));
        assertFalse(new Eq(new SingleColumn("colA"), new QuotedString("'2'")).evaluate(row));
    }

}
