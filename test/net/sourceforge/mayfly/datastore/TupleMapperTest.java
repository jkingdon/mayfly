package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

public class TupleMapperTest extends TestCase {
    
    public void testBasics() throws Exception {
        Row initial = 
            new TupleBuilder()
                .appendColumnCell("a", new LongCell(5))
                .appendColumnCell("c", new LongCell(5))
                .asRow();
        TupleMapper mapper = new TupleMapper(initial);
        /** Need not handle the "A" case; in real life the
         * {@link Column} is looked up rather than constructed.
         */
        mapper.put(new Column("a"), new StringCell("hi"));
        mapper.put(new Column("b"), new LongCell(77));
        Row result = mapper.asRow();
        assertEquals(3, result.size());
    }

}
