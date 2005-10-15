package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import java.util.*;

public class EnumerableTest extends TestCase {
    public void testSelect() throws Exception {
        Strings result =
            new Strings("a", "b", "c")
                .select(
                    new Selector<String>() {
                        public boolean evaluate(String candidate) {
                            return candidate.equals("a") || candidate.equals("c");
                        }
                    }
                );

        assertEquals(new Strings("a", "c"), result);
    }

    public void testCollect() throws Exception {
        Collection<String> collected = new Strings("a", "b")
            .collect(
                new Transformer<String, String>() {
                    public String transform(String from) {
                        return from + "x";
                    }
                }
            );

        assertEquals(Arrays.asList("ax", "bx"), collected);
    }


    public void testFindAndExists() throws Exception {
        assertEquals("b",
                     new Strings("a", "b", "c")
                        .find(
                            new Selector<String>() {
                                public boolean evaluate(String candidate) {
                                    return candidate.equals("b");
                                }
                            }
                        )
        );

        assertTrue(
            new Strings("a", "b", "c")
                        .exists(
                            new Selector<String>() {
                                public boolean evaluate(String candidate) {
                                    return candidate.equals("b");
                                }
                            }
                        )
        );
    }

    class Strings extends Enumerable<Strings, String> {
        private Collection<String> strings;

        public Strings(String... strings) {
            this.strings = Arrays.asList(strings);
        }

        public Iterator<String> iterator() {
            return strings.iterator();
        }

        protected Strings createNew(Collection<String> items) {
            return new Strings(items.toArray(new String[items.size()]));
        }

    }
}