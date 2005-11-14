package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;

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

    public void testMatches2() throws Exception {
        assertTrue(new Column(new TableIdentifier("FOO"), "aaa").matches2("foo", "aAa"));
        assertFalse(new Column(new TableIdentifier("FOO"), "aaa").matches2("fOo", "aaB"));

        assertTrue(new Column(new TableIdentifier("FOO"), "aaa").matches2("fOO", "aAa"));

        assertFalse(new Column(new TableIdentifier("FOO"), "a").matches2("bar", "a"));
        assertFalse(new Column(new TableIdentifier("bar"), "a").matches2("foo", "a"));
        assertTrue(new Column(new TableIdentifier("FOO"), "a").matches2("Foo", "a"));

        assertTrue(new Column(new TableIdentifier("FoO"), "a").matches2("Foo", "a"));
    }

}