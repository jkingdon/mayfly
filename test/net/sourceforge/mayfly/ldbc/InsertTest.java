package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import java.util.*;

public class InsertTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Collections.singletonList("a"),
                Collections.singletonList(new Long(5))
            ),
            Insert.fromTree(Tree.parse("insert into foo (a) values (5)"))
        );
    }

}
