package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;

public class ColumnTest extends TestCase {

    public void testMatches() throws Exception {
        assertTrue(new Column("aaa").matches(null, "aAa"));
        assertFalse(new Column("aaa").matches(null, "aaB"));
        
        assertFalse(new Column("a").matches("foo", "a"));

        assertTrue(new Column("FOO", "aaa").matches(null, "aAa"));
        assertFalse(new Column("FOO", "aaa").matches(null, "aaB"));

        assertTrue(new Column("FOO", "aaa").matches(null, "aAa"));

        assertFalse(new Column("FOO", "a").matches("bar", "a"));
        assertTrue(new Column("FOO", "a").matches("Foo", "a"));
    }

}