package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.CaseInsensitiveString;

public class TupleMapperTest extends TestCase {
    
    public void testBasics() throws Exception {
        Row initial = 
            new TupleBuilder()
                .append("a", new LongCell(5))
                .append("c", new LongCell(5))
                .asRow();
        TupleMapper mapper = new TupleMapper(initial);
        mapper.put(new Column("a"), new StringCell("hi"));
        mapper.put(new Column("b"), new LongCell(77));
        Row result = mapper.asRow();
        assertEquals(3, result.columnCount());
    }
    
    public void testAdd() throws Exception {
        TupleMapper tuple = new TupleMapper();
        tuple.add("a", new LongCell(77));
        
        try {
            tuple.add("A", new StringCell("hi"));
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate column A", e.getMessage());
        }
    }
    
    public void testReplaceDifferingCase() throws Exception {
        TupleMapper tuple = new TupleMapper();
        tuple.put("a", new LongCell(7));
        tuple.put("A", new LongCell(8));
        Row result = tuple.asRow();
        assertEquals(1, result.columnCount());
    }
    
    public void testHas() throws Exception {
        TupleMapper tuple = new TupleMapper();
        tuple.put("a", new LongCell(7));
        assertTrue(tuple.hasColumn(new CaseInsensitiveString("a")));
        assertTrue(tuple.hasColumn(new CaseInsensitiveString("A")));
        assertFalse(tuple.hasColumn(new CaseInsensitiveString("aa")));
    }

}
