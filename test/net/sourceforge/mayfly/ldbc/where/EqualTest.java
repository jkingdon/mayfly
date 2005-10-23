package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class EqualTest extends TestCase {
    public void testColumnAndQuotedString() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .entry(new Column("colB"), new Cell("2"))
                .asImmutable()
        );

        assertTrue(new Equal(new Column("colA"), new QuotedString("'1'")).evaluate(row));
        assertFalse(new Equal(new Column("colA"), new QuotedString("'2'")).evaluate(row));
    }
}