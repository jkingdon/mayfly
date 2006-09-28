package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

import java.util.Collections;

public class CommandTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new DropTable("FOO", false), Command.fromSql("drop table FOO"));
        assertEquals(new CreateTable("Foo", Collections.singletonList("a")),
            Command.fromSql("Create Table Foo (a integer)"));
    }
    
}
