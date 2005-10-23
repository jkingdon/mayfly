package net.sourceforge.mayfly.util;

import junit.framework.TestCase;

import java.util.*;

public class MTest extends TestCase {
    public void testEntry() throws Exception {
        Map expected = new HashMap();
        expected.put("a", "b");
        expected.put("c", "d");

        assertEquals(expected,
                     new M()
                        .entry("a", "b")
                        .entry("c", "d")
        );
    }

    public void testSubMap() throws Exception {
        L keysWanted = new L()
                         .append("a")
                         .append("b");
        assertEquals(
            new M()
                .entry("a", "x")
                .entry("b", "y"),
            new M()
                .entry("a", "x")
                .entry("b", "y")
                .entry("c", "z")
                .subMap(keysWanted)
        );
    }

}