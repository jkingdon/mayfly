package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

public class SelectTest extends TestCase {
    public void testParse() throws Exception {
        assertEquals(
            new Select(
                new Dimensions()
                    .add(new Dimension("foo", "f"))
                    .add(new Dimension("bar", "b"))
            ),
            Select.fromTree(Tree.parse("select * from foo f, bar b"))
        );
    }
}