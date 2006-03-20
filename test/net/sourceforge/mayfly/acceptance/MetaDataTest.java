package net.sourceforge.mayfly.acceptance;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * There is a question of whether our JDBC metadata should somehow be
 * limited to reflect what is portable against various JDBC drivers.
 * For now, it is merely limited by what we've gotten around to
 * implementing.  Which, at the time of writing, is nothing.
 */
public class MetaDataTest extends SqlTestCase {

    public void testNothing() {
    }

    public void xtestNoTables() throws Exception {
        // not sure about null vs "" for schema, catalog.
        // But null is clearly what hypersonic wants for "across all schemas/catalogs".
        ResultSet tables = connection.getMetaData().getTables(null, null, "", null);
        assertFalse(tables.next());
        tables.close();
    }

    public void xtestOneTable() throws Exception {
        Statement statement = connection.createStatement();
        assertEquals(0, statement.executeUpdate("create table foo (a integer)"));
        statement.close();
        connection.commit();

        ResultSet tables = connection.getMetaData().getTables(null, null, "%", null);
        assertTrue("first row", tables.next());
        assertEquals("foo", tables.getString("TABLE_NAME"));
        assertFalse("no second row", tables.next());
        tables.close();
    }

}
