package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

public class EqualTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
                new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                new Parser("name='steve'").parseCondition().asBoolean()
        );
    }

    public void testColumnAndQuotedString() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
        );

        assertTrue(new Equal(new SingleColumn("colA"), new QuotedString("'1'")).evaluate(row));
        assertFalse(new Equal(new SingleColumn("colA"), new QuotedString("'2'")).evaluate(row));
    }

}
