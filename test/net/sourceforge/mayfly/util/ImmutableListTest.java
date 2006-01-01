package net.sourceforge.mayfly.util;

import junit.framework.*;

import java.util.*;

public class ImmutableListTest extends TestCase {
    
    public void testUnmodifiable() throws Exception {
        List list = new ImmutableList(Arrays.asList(new String[] { "a", "b", "c" }));
        try {
            list.add("d");
            fail();
        } catch (UnsupportedOperationException expected) {
            
        }
    }
    
    public void testReadAccess() throws Exception {
        List list = new ImmutableList(Arrays.asList(new String[] { "a", "b", "c" }));
        assertEquals(3, list.size());
        assertEquals("b", list.get(1));
    }
    
    public void testConstructorCopies() throws Exception {
        String[] backingArray = new String[] { "a", "b", "c" };
        List list = new ImmutableList(Arrays.asList(backingArray));
        backingArray[1] = "BOO!";
        assertEquals("b", list.get(1));
    }
    
    public void testWith() throws Exception {
        ImmutableList list = new ImmutableList();

        ImmutableList oneElement = list.with("a");
        assertEquals(0, list.size());
        assertEquals(1, oneElement.size());
        assertEquals("a", oneElement.get(0));
        
        List addToNonEmpty = oneElement.with("b");
        assertEquals(2, addToNonEmpty.size());
        assertEquals("a", addToNonEmpty.get(0));
        assertEquals("b", addToNonEmpty.get(1));
    }

}
