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

    public void testCaseInsensitive() throws Exception {
        assertTrue(new M.CaseInsensitiveKeyIs("aAa").evaluate(new MyEntry("aAa")));
        assertTrue(new M.CaseInsensitiveKeyIs("aAa").evaluate(new MyEntry("AAA")));
        assertTrue(new M.CaseInsensitiveKeyIs("aAa").evaluate(new MyEntry("aaa")));
        assertFalse(new M.CaseInsensitiveKeyIs("aAa").evaluate(new MyEntry("zzz")));
        
    }

    class MyEntry implements Map.Entry {
        private Object key;

        public MyEntry(Object key) {
            this.key = key;
        }

        public Object getKey() {
            return key;
        }

        public void setKey(Object key) {
            this.key = key;
        }

        public Object getValue() {
            return null;
        }

        public Object setValue(Object value) {
            return null;
        }
    }

}