package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.ImmutableList;

public class IndexesTest extends TestCase {
    
    public void testRejectDuplicatesInListConstructor() throws Exception {
        Index one = new Index("foo", null);
        Index two = new Index("foo", null);
        try {
            new Indexes(ImmutableList.fromElements(one, two));
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate index foo", e.getMessage());
        }
    }

    public void testRejectDuplicatesInWith() throws Exception {
        Index one = new Index("foo", null);
        Index two = new Index("foo", null);
        try {
            new Indexes().with(one).with(two);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate index foo", e.getMessage());
        }
    }

    public void testCaseInsensitive() throws Exception {
        Index one = new Index("foo", null);
        Index two = new Index("FOO", null);
        try {
            new Indexes().with(one).with(two);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate index FOO", e.getMessage());
        }
    }

    public void testJustOne() throws Exception {
        Index one = new Index("foo", null);
        new Indexes().with(one);
    }
    
    public void testNoName() throws Exception {
        Index nameless = new Index(null, null);
        Index nameless2 = new Index(null, null);
        new Indexes().with(nameless).with(nameless2);
    }
    
    public void testWithout() throws Exception {
        Index one = new Index("foo", null);
        Index two = new Index("BAR", null);
        Index nameless = new Index(null, null);
        Indexes before = new Indexes().with(one).with(two).with(nameless);

        assertEquals(3, before.indexCount());
        Indexes after = before.without("bar");
        assertEquals(2, after.indexCount());
        
        try {
            before.without("baz");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("no index baz", e.getMessage());
        }
    }

}
