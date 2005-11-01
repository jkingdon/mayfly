package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;

import net.sourceforge.mayfly.util.*;

public class ColumnTest extends TestCase {

    public void testEquality() throws Exception {
        A.assertEquals(new Column("aaa"), new Column("aAa"));
        A.assertNotEquals(new Column("aaa"), new Column("aaB"));
    }

}