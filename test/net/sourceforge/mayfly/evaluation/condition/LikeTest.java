package net.sourceforge.mayfly.evaluation.condition;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;

public class LikeTest extends TestCase {
    
    public void testNothingWild() throws Exception {
        assertTrue(Like.compare("abc", "abc"));
        assertFalse(Like.compare("abc", "abcd"));
        assertFalse(Like.compare("abc", "ab"));
        assertFalse(Like.compare("abc", "bc"));
    }
    
    public void testCaseSensitive() throws Exception {
        assertFalse(Like.compare("abc", "ABC"));
        assertFalse(Like.compare("ABC", "abc"));
    }
    
    public void testPattern() throws Exception {
        assertTrue(Like.compare("abc", "a%"));
        assertTrue(Like.compare("abc", "%c"));
        assertTrue(Like.compare("abc", "%b%"));
    }

    public void testSpecialCharacters() throws Exception {
        assertTrue(Like.compare("a%", "a%"));
        assertTrue(Like.compare(
            " !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~", 
            " !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~"));
        assertFalse(Like.compare(
            " !\"#$&'()+,-./:;<=>?@[\\]^_`{|}~", 
            " !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~"));
    }
    
    public void testQuote() throws Exception {
        assertEquals("\\?", Like.quote("?"));
        assertEquals("\\.", Like.quote("."));
        assertEquals("%", Like.quote("%"));
        assertEquals("a", Like.quote("a"));
    }
    
    public void testNulls() throws Exception {
        Like like = new Like(null, null);
        assertFalse(like.compare(new StringCell("hi"), NullCell.INSTANCE));
        assertFalse(like.compare(NullCell.INSTANCE, new StringCell("hi")));
        assertTrue(like.compare(new StringCell("hi"), new StringCell("hi")));
    }

}
