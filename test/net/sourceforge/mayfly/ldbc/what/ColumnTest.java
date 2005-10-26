package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;

public class ColumnTest extends TestCase {
    public void testEquality() throws Exception {
        A.assertEquals(new Column("aaa"), new Column("aAa"));
        A.assertNotEquals(new Column("aaa"), new Column("aaB"));
    }

    public void testRowTransform() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .entry(new Column("colB"), new Cell("2"))
                .asImmutable()
        );

        assertEquals(new Cell("1"), new Column("colA").transform(row));
        assertEquals(new Cell("2"), new Column("colB").transform(row));
    }
}