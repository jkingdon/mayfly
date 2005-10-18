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
}