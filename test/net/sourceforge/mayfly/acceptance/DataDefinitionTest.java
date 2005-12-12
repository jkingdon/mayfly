package net.sourceforge.mayfly.acceptance;

import java.sql.*;
    
public class DataDefinitionTest extends SqlTestCase {
    public void testDuplicateColumnName() throws Exception {
        try {
            execute("create table foo (Id integer, Id integer)");
            fail();
        } catch (SQLException e) {
            assertMessage("duplicate column Id", e);
        }
    }

}
