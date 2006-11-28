package net.sourceforge.mayfly.util;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

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

}
