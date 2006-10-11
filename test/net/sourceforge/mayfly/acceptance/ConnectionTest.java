package net.sourceforge.mayfly.acceptance;

import java.sql.SQLWarning;

public class ConnectionTest extends SqlTestCase {
    
    public void testWarnings() throws Exception {
        execute("create table foo (x integer)");
        SQLWarning warnings = connection.getWarnings();
        assertNull(warnings);
        connection.clearWarnings();
    }

}
