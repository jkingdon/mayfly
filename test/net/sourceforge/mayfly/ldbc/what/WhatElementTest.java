package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.*;

public class WhatElementTest extends TestCase {

    public void testSingleColumn() {
        WhatElement element = new SingleColumn("table", "col");
        Columns columns = element.columns(null);
        assertEquals(1, columns.size());
        Column column = columns.get(0);
        assertEquals("table", column.tableOrAlias());
        assertEquals("col", column.columnName());
    }

}
