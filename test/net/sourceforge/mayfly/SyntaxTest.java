package net.sourceforge.mayfly;

import java.sql.*;

public class SyntaxTest extends SqlTestCase {

    public void testBadCommand() throws Exception {
        try {
            execute("PICK NOSE");
            fail();
        } catch (SQLException expected) {
            assertMessage("unexpected token: PICK", expected);
        }
    }

    public void testCommandsAreCaseInsensitive() throws Exception {
        try {
            execute("DrOp tAbLe FOo");
            fail();
        } catch (SQLException expected) {
            assertMessage("no such table FOo", expected);
        }
    }

    public void testColumnsMissingOnCreate() throws Exception {
        try {
            execute("create table foo");
            fail();
        } catch (SQLException expected) {
            // Really should try to do better than the JDBC/ANTLR "unexpected token: null"
            //assertMessage("must specify columns on create", expected);
        }
    }
    
    // Apparently neither ldbc nor jsqlparser can quote identifiers
    // at all.  Surprising.
    public void xtestXAndQuoting() throws Exception {
        execute("create table \"foo\" (\"x\" integer)");
        //query("select x from foo");
        query("select \"x\" from \"foo\"");
        //query("select \"x\" from foo"); // not legal, at least in hypersonic
    }

}
