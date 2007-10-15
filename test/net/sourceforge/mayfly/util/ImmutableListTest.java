package net.sourceforge.mayfly.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

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
    
    public void testWithAll() throws Exception {
        ImmutableList list = new ImmutableList().with("a");
        ImmutableList actual = list.withAll(new L().append("b").append("c"));
        L expected = new L().append("a").append("b").append("c");
        assertEquals(expected, actual);
    }
    
    public void testWithout() throws Exception {
        ImmutableList list = new ImmutableList().with("a").with("b").with("c");
        ImmutableList result = list.without(1);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("c", result.get(1));
    }
    
    public void testWithAtIndex() throws Exception {
        ImmutableList list = new ImmutableList().with("a").with("c");
        ImmutableList result = list.with(1, "b");
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }
    
}
