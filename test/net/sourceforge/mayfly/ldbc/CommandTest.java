package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import java.util.*;

public class CommandTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new DropTable("FOO"), Command.fromTree(Tree.parse("drop table FOO")));
        assertEquals(new CreateTable("Foo", Collections.singletonList("a")),
            Command.fromTree(Tree.parse("Create Table Foo (a integer)")));
        assertEquals(
            new Insert(new InsertTable("foo"),
                Collections.singletonList("a"),
                Collections.singletonList(new Long(5))
            ), 
            Command.fromTree(Tree.parse("insert into foo (a) values (5)")));
    }
}
