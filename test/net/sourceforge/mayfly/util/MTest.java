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

    public void testPlus() throws Exception {
        Map expected = new HashMap();
        expected.put("a", "b");
        expected.put("c", "d");


        assertEquals(
            new M()
                .entry("a", "b")
                .entry("c", "d"),
            new M()
                .entry("a", "b")
                .plus(
                    new M()
                        .entry("c", "d")
                )
        );
    }
}