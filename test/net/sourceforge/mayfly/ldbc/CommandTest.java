package net.sourceforge.mayfly.ldbc;

import junit.framework.TestCase;

import java.util.Collections;

public class CommandTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new DropTable("FOO"), Command.fromSql("drop table FOO"));
        assertEquals(new CreateTable("Foo", Collections.singletonList("a")),
            Command.fromSql("Create Table Foo (a integer)"));
        assertEquals(
            new Insert(new InsertTable("foo"),
                Collections.singletonList("a"),
                Collections.singletonList(new Long(5))
            ), 
            Command.fromSql("insert into foo (a) values (5)"));
    }
    
}
