package net.sourceforge.mayfly;

import java.sql.*;


public class DataTypeTest extends SqlTestCase {

    public void testTypes() throws Exception {
        execute("create table foo (a integer)");
    }
    
    public void testStrings() throws Exception {
        execute("create table foo (color varchar, size varchar)");
        execute("insert into foo (color, size) values ('red', 'medium')");

        ResultSet results = query("select size, color from foo");
        assertTrue(results.next());
        assertEquals("medium", results.getString(1));
        assertEquals("red", results.getString("color"));
        assertFalse(results.next());
    }

    public void testAsciiPunctuation() throws Exception {
        execute("create table foo (value varchar)");
        execute("insert into foo (value) values (' !\"#$%&''()*+,-./:;<=>?@[\\]^_`{|}~')");

        ResultSet results = query("select value from foo where value = ' !\"#$%&''()*+,-./:;<=>?@[\\]^_`{|}~'");
        assertTrue(results.next());
        assertEquals(" !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~", results.getString(1));
        assertFalse(results.next());
    }

}
