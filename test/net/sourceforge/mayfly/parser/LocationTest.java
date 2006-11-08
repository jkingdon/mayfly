package net.sourceforge.mayfly.parser;

import junit.framework.TestCase;

public class LocationTest extends TestCase {
    
    public void testCombine() throws Exception {
        Location one = new Location(5, 40, 7, 30);
        Location two = new Location(8, 35, 9, 32);
        Location combined = one.combine(two);

        assertEquals(5, combined.startLineNumber);
        assertEquals(40, combined.startColumn);
        assertEquals(9, combined.endLineNumber);
        assertEquals(32, combined.endColumn);
    }
    
    public void testKnown() throws Exception {
        Location one = new Location(5, 40, 7, 30);
        assertTrue(one.knowStart());
        assertTrue(one.knowEnd());
        assertFalse(Location.UNKNOWN.knowStart());
        assertFalse(Location.UNKNOWN.knowEnd());
        
        Location mixed = one.combine(Location.UNKNOWN);
        assertTrue(mixed.knowStart());
        assertFalse(mixed.knowEnd());
        
        Location otherMixed = Location.UNKNOWN.combine(one);
        assertFalse(otherMixed.knowStart());
        assertTrue(otherMixed.knowEnd());
    }
    
    public void testContains() throws Exception {
        Location one = new Location(5, 40, 7, 30);
        assertFalse(one.contains(4, 73));
        assertFalse(one.contains(5, 39));
        assertTrue(one.contains(5, 40));
        assertTrue(one.contains(6, 77));
        assertTrue(one.contains(7, 29));
        assertFalse(one.contains(7, 30));
        assertFalse(one.contains(8, 1));
    }

    public void testSingleLineContains() throws Exception {
        Location one = new Location(5, 20, 5, 30);
        assertFalse(one.contains(4, 73));
        assertFalse(one.contains(5, 19));
        assertTrue(one.contains(5, 20));
        assertTrue(one.contains(5, 29));
        assertFalse(one.contains(5, 30));
        assertFalse(one.contains(6, 1));
    }

}
