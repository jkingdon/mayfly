package net.sourceforge.mayfly;

import java.sql.*;


public class DataTypeTest extends SqlTestCase {

    public void testTypes() throws Exception {
        execute("create table foo (a integer)");
    }
    
    public void testStrings() throws Exception {
        execute("create table foo (color varchar, size varchar)");
        execute("insert into foo (color, size) values ('red', 'medium')");

        {
            ResultSet results = query("select size, color from foo");
            assertTrue(results.next());
            assertEquals("medium", results.getString(1));
            assertEquals("red", results.getString("color"));
            assertFalse(results.next());
            results.close();
        }

        {
            ResultSet results = query("select size, color from foo");
            assertTrue(results.next());
            assertEquals("medium", results.getObject(1));
            assertEquals("red", results.getObject("color"));
            assertFalse(results.next());
            results.close();
        }
    }

    public void testAsciiPunctuation() throws Exception {
        execute("create table foo (value varchar)");
        execute("insert into foo (value) values (' !\"#$%&''()*+,-./:;<=>?@[\\]^_`{|}~')");

        ResultSet results = query("select value from foo where value = ' !\"#$%&''()*+,-./:;<=>?@[\\]^_`{|}~'");
        assertTrue(results.next());
        assertEquals(" !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~", results.getString(1));
        assertFalse(results.next());
    }

    public void testInteger() throws Exception {
        execute("create table foo (waist integer, inseam integer)");
        execute("insert into foo (waist, inseam) values (30, 32)");

        {
            ResultSet results = query("select waist, inseam from foo");
            assertTrue(results.next());
            assertEquals(30, results.getInt(1));
            assertEquals(32, results.getInt("inseam"));
            assertFalse(results.next());
            results.close();
        }

//        {
//            ResultSet results = query("select waist, inseam from foo");
//            assertTrue(results.next());
//            // Are these supposed to be Integer? Long? Hypersonic says Integer
//            assertEquals(new Integer(30), results.getObject(1));
//            assertEquals(new Integer(32), results.getObject("inseam"));
//            assertFalse(results.next());
//            results.close();
//        }
    }

}
