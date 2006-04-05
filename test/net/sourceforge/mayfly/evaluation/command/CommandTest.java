package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.CreateTable;
import net.sourceforge.mayfly.evaluation.command.DropTable;
import net.sourceforge.mayfly.evaluation.command.Insert;
import net.sourceforge.mayfly.evaluation.command.InsertTable;

import java.util.Collections;

public class CommandTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new DropTable("FOO", false), Command.fromSql("drop table FOO"));
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
