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

    public void testSlurp() throws Exception {
        assertEquals(
            new L()
                .append("a")
                .append("b")
                .append("c"),
            new L()
                .append("a")
                .slurp(new IterableCollection(new L()
                                                .append("b")
                                                .append("c")))
        );
    }

}
