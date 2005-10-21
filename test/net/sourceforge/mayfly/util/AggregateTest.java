package net.sourceforge.mayfly.util;

import junit.framework.*;

import java.util.*;

public class AggregateTest extends TestCase {
    public void testSelect() throws Exception {
        Strings result =
            (Strings)
                new Strings(new String[]{"a", "b", "c"})
                .select(
                    new Selector() {
                        public boolean evaluate(Object candidate) {
                            return candidate.equals("a") || candidate.equals("c");
                        }
                    }
                );

        assertEquals(new Strings(new String[]{"a", "c"}), result);
    }

    public void testCollect() throws Exception {
        Collection collected = new Strings(new String[]{"a", "b"})
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
                     new Strings(new String[]{"a", "b", "c"})
                        .find(
                            new Selector() {
                                public boolean evaluate(Object candidate) {
                                    return candidate.equals("b");
                                }
                            }
                        )
        );

        assertTrue(
            new Strings(new String[]{"a", "b", "c"})
                        .exists(
                            new Selector() {
                                public boolean evaluate(Object candidate) {
                                    return candidate.equals("b");
                                }
                            }
                        )
        );
    }

    class Strings extends Aggregate {
        private Collection strings;

        public Strings(String[] strings) {
            this.strings = Arrays.asList(strings);
        }

        public Iterator iterator() {
            return strings.iterator();
        }

        protected Object createNew(Iterable items) {
            List list = asList(items);
            return new Strings((String[]) list.toArray(new String[list.size()]));
        }

    }
}