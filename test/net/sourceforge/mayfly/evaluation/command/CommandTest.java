package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Collections;

public class CommandTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new DropTable("FOO", false), Command.fromSql("drop table FOO"));
        assertEquals(new CreateTable("Foo", Collections.singletonList("a")),
            Command.fromSql("Create Table Foo (a integer)"));
        assertEquals(
            new Insert(new InsertTable("foo"),
                ImmutableList.singleton("a"),
                ImmutableList.singleton(new LongCell(5))
            ), 
            Command.fromSql("insert into foo (a) values (5)"));
    }
    
}
