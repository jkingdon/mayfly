package net.sourceforge.mayfly.util;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;

import java.util.Arrays;
import java.util.Collection;

public class AggregateTest extends TestCase {
    public void testSelect() throws Exception {
        Strings result =
            (Strings)
                new Strings("a", "b", "c")
                .select(
                    new Selector() {
                        public boolean evaluate(Object candidate) {
                            return candidate.equals("a") || candidate.equals("c");
                        }
                    }
                );

        assertEquals(new Strings("a", "c"), result);
    }

    public void testCollect() throws Exception {
        Collection collected = new Strings("a", "b")
            .collect(
                new Transformer() {
                    public Object transform(Object from) {
                        return from + "x";
                    }
                }
            );

        assertEquals(Arrays.asList(new Object[]{"ax", "bx"}),
                     collected);
    }


    public void testFindAndExists() throws Exception {
        assertEquals("b",
                     new Strings("a", "b", "c")
                        .findFirst(
                            new Selector() {
                                public boolean evaluate(Object candidate) {
                                    return candidate.equals("b");
                                }
                            }
                        )
        );

        assertTrue(
            new Strings("a", "b", "c")
                        .exists(
                            new Selector() {
                                public boolean evaluate(Object candidate) {
                                    return candidate.equals("b");
                                }
                            }
                        )
        );
    }

    public void testPlus() throws Exception {
        assertEquals(
            new Strings("a", "b"),
            new Strings("a").plus(new Strings("b"))
        );
    }

    public void testZipper() throws Exception {
        assertEquals(
            new M()
                .entry("a", "x")
                .entry("b", "y")
                .entry("c", "z"),
            new Strings("a", "b", "c").zipper(new Strings("x", "y", "z"))
        );

        Transformer append1 = new Transformer() {
            public Object transform(Object from) {
                return from.toString() + "1";
            }
        };

        Transformer append2 = new Transformer() {
            public Object transform(Object from) {
                return from.toString() + "2";
            }
        };

        assertEquals(
            new M()
                .entry("a1", "x2")
                .entry("b1", "y2")
                .entry("c1", "z2"),
            new Strings("a", "b", "c").zipper(append1, new Strings("x", "y", "z"), append2)
        );
    }

    public void testWith() throws Exception {
        Strings original = new Strings("a");
        Strings result = (Strings) original.with("b");

        assertEquals(
            new Strings("a", "b"),
            result
        );

        assertFalse(original==result);
    }

    public void testIndex() throws Exception {
        assertEquals("b", new Strings("a", "b", "c").element(1));
    }

    public void testSize() throws Exception {
        assertEquals(3, new Strings("a", "b", "c").size());
    }

    public void testSubtract() throws Exception {
        assertEquals(
            new Strings("b"),
            new Strings("a", "b").subtract(new Strings("a"))
        );

        assertEquals(
            new Strings("b"),
            new Strings("a", "b").subtract(new Strings("a", "c"))
        );
    }

    public void testHasContents() throws Exception {
        assertTrue(new Strings("a").hasContents());
        assertFalse(new Strings().hasContents());
    }

    public void testCartesianJoin() throws Exception {
        L leftSide =
            new L()
                .append(new Strings("a"))
                .append(new Strings("b"));

        L rightSide =
            new L()
                .append(new Strings("x", "x"))
                .append(new Strings("y", "y"))
                .append(new Strings("z", "z"));

        assertEquals(
            new L()
                .append(new Strings("a", "x", "x"))
                .append(new Strings("a", "y", "y"))
                .append(new Strings("a", "z", "z"))
                .append(new Strings("b", "x", "x"))
                .append(new Strings("b", "y", "y"))
                .append(new Strings("b", "z", "z")),
            leftSide.cartesianJoin(rightSide)
        );
    }

    public void testElementsAt() throws Exception {
        assertEquals(
            new Strings("a", "c"),
            new Strings("a", "b", "c").elements(new int[]{0, 2})
        );
    }

    public void testNotFoundException() throws Exception {
        try {
            new Strings("a", "b", "c")
                .messageIfNotFound("couldn''t find {0}, try again")
                .findFirst(new Selector() {
                    public boolean evaluate(Object candidate) {
                        return candidate.equals("d");
                    }

                    public String toString() {
                        return "d";
                    }
                });
        } catch (MayflyException ex) {
            assertEquals("couldn't find d, try again", ex.getMessage());
        }
    }
    
    public void testToString() throws Exception {
        assertEquals("[a, b]", new Strings("a", "b").toString());
    }

}
