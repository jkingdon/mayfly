package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

public class DropTableTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
            new DropTable("Foo"),
            DropTable.dropTableFromTree(Tree.parse("drop taBLe Foo"))
        );
    }
}
