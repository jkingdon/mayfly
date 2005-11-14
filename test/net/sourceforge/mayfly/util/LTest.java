package net.sourceforge.mayfly.util;

import junit.framework.TestCase;

import java.util.*;

public class LTest extends TestCase {
    public void testAdd() throws Exception {
        L list = new L();

        //not a great method name.  we need some other method name besides add, and it can't be too verbose.

        list
            .append("a")
            .append("b");

        List expected = new ArrayList();
        expected.add("a");
        expected.add("b");

        assertEquals(expected, list);
    }

    public void testSelectObjectsOfClass() throws Exception {
        L list = new L();

        list
            .append(new Integer(1))
            .append(new Integer(2))
            .append("a")
            .append("b");

        assertEquals(
            new L()
                .append("a")
                .append("b"),
            list.selectObjectsThatAre(String.class)
        );

        assertEquals(
            new L()
                .append(new Integer(1))
                .append(new Integer(2)),
            list.selectObjectsThatAre(Integer.class)
        );
    }
    
    public void testToString() throws Exception {
        List arrayList = new ArrayList();
        arrayList.add(new Integer(7));
        arrayList.add(new Integer(8));
        assertEquals("[7, 8]", arrayList.toString());

        L list = new L(arrayList);
        assertEquals("[7, 8]", list.toString());
    }

    public void testAddIterable() throws Exception {
        assertEquals(
            new L()
                .append("a")
                .append("b")
                .append("c"),
            new L()
                .append("a")
                .addAll(new IterableCollection(new L()
                                                .append("b")
                                                .append("c")))
        );
    }

    public void testIndexToElementMap() throws Exception {
        assertEquals(
            new M()
                .entry(new Integer(0), "a")
                .entry(new Integer(1), "b"),
            new L()
                .append("a")
                .append("b")
                .asIndexToElementMap()
        );
    }
    
    public void testSubListToEnd() throws Exception {
        L abc = new L(Arrays.asList(new String[] {"a", "b", "c"}));
        assertEquals(
            new L(Arrays.asList(new String[] {"b", "c"})),
            abc.subList(1)
        );
    }

    public void testAsUnmodifiable() throws Exception {

        L original = new L().append("a");

        L unmodifiable = original.asUnmodifiable();

        try {
            unmodifiable.append("b");
            fail();
        } catch (UnsupportedOperationException expected) {}

        assertEquals(new L().append("a"), unmodifiable);
        
        original.append("c");

        // Here's why this is less safe than making an ImmutableList.
        assertEquals(new L().append("a").append("c"), unmodifiable);
    }

}
