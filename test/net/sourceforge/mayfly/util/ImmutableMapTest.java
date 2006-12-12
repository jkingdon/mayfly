package net.sourceforge.mayfly.util;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ImmutableMapTest extends TestCase {
    
    public void testUnmodifiable() throws Exception {
        Map map = new ImmutableMap();
        try {
            map.put("a", "A");
            fail();
        }
        catch (UnsupportedOperationException expected) {
            
        }
    }
    
    public void testReadAccess() throws Exception {
        Map map = new ImmutableMap(Collections.singletonMap("a", "A"));
        assertEquals(1, map.size());
        assertEquals("A", map.get("a"));
        assertFalse(map.containsKey("b"));
    }
    
    public void testConstructorCopies() throws Exception {
        Map original = new TreeMap();
        original.put("a", "A");
        original.put("b", "B");
        Map copy = new ImmutableMap(original);
        original.put("b", "boo!");
        assertEquals("B", copy.get("b"));
    }
    
    public void testWith() throws Exception {
        ImmutableMap map = new ImmutableMap();
        
        ImmutableMap oneEntry = map.with("a", "A");
        assertEquals(0, map.size());
        assertEquals(1, oneEntry.size());
        assertEquals("A", oneEntry.get("a"));
        
        Map addToNonEmpty = oneEntry.with("b", "B");
        assertEquals(2, addToNonEmpty.size());
        assertEquals("A", addToNonEmpty.get("a"));
        assertEquals("B", addToNonEmpty.get("b"));
    }
    
    public void testWithout() throws Exception {
        ImmutableMap twoEntries = new ImmutableMap().with("a", "A").with("b", "B");
        Map oneEntry = twoEntries.without("b");
        assertEquals(1, oneEntry.size());
        assertEquals("A", oneEntry.get("a"));
        
        try {
            twoEntries.without("c");
            fail();
        } catch (NoSuchKeyException expected) {
            
        }
    }
    
    public void testReplaceAndOrder() throws Exception {
        ImmutableMap twoEntries = 
            new ImmutableMap().with("a", "A").with("b", "B");

        {
            ImmutableMap changeInPlace = twoEntries.with("a", "AAA");
            Iterator iter = changeInPlace.entrySet().iterator();
            assertEquals("AAA", ((Map.Entry)iter.next()).getValue());
            assertEquals("B", ((Map.Entry)iter.next()).getValue());
            assertFalse(iter.hasNext());
        }

    }

}
