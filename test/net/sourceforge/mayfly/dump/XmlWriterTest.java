package net.sourceforge.mayfly.dump;

import junit.framework.TestCase;

public class XmlWriterTest extends TestCase {

    private XmlWriter out;

    @Override
    public void setUp() {
        out = new XmlWriter();
    }

    public void testBasics() {
        out.startTag("html");
        out.endTag("html");
        assertEquals("<html></html>", out.getOutput());
    }
    
    public void testText() {
        out.startTag("html");
        out.text("don't\"<&>");
        out.endTag("html");
        assertEquals("<html>don't\"&lt;&amp;&gt;</html>", out.getOutput());
    }
    
    public void testAttribute() {
        out.startTag("html", "characters", "<&>\"don't");
        out.endTag("html");
        assertEquals("<html characters=\"&lt;&amp;&gt;&quot;don't\"></html>", out.getOutput());
    }
    
    public void testMultipleAttributes() {
        out.startTag("form", new String[] {"action", "launch", "method", "nuke"});
        out.endTag("form");
        assertEquals("<form action=\"launch\" method=\"nuke\"></form>", out.getOutput());
    }
    
    public void testMultipleAttributesSingleTag() {
        out.singleTag("img", new String[] {"src", "foo.png", "alt", "Wombat Porn"});
        assertEquals("<img src=\"foo.png\" alt=\"Wombat Porn\" />", out.getOutput());
    }
    
    public void testSingleTag() {
        out.singleTag("br");
        assertEquals("<br />", out.getOutput());
    }

    public void testSingleTagWithAttribute() {
        out.singleTag("input", "name", "phoneNumber");
        assertEquals("<input name=\"phoneNumber\" />", out.getOutput());
    }
    
    public void testUnclosed() {
        out.startTag("html");
        try {
            out.getOutput();
            fail();
        }
        catch (XmlWriterException e) {
            assertEquals("unclosed element html", e.getMessage());
        }
    }
    
    public void testMismatched() {
        out.startTag("p");
        try {
            out.endTag("body");
            fail();
        }
        catch (XmlWriterException e) {
            assertEquals("end tag body does not match start tag p", e.getMessage());
        }
    }
    
    public void testLegalNameCharacters() {
        /* Leading colon is legal according to XML 1.0 spec, but the namespace spec
         * does not appear to allow it. */
        out.startTag(":foo");
        out.startTag("_bar");
        out.singleTag("x:y-z_w.7e\u0300");
        out.endTag("_bar");
        out.endTag(":foo");
        assertEquals("<:foo><_bar><x:y-z_w.7e\u0300 /></_bar></:foo>", out.getOutput());
    }
    
    public void testBadCharacterInName() throws Exception {
        /* Of course, this is just a small subset of the characters which are illegal
         * accoding to the XML spec.  But these ones seem like the ones most likely
         * to create a security hole or other really strange stuff.
         * 
         * As for the rest, the rules are complicated and differ between XML 1.0
         * and 1.1, so maybe this is one of those things that isn't a big enough
         * problem in practice to worry about enforcing.
         */
        checkBadCharacter("Bad character < in start tag a<b", "a<b");
        checkBadCharacter("Bad character & in start tag this&that", "this&that");
        checkBadCharacter("Bad character > in start tag x>y", "x>y");
        checkBadCharacter("Bad character ' in start tag don't", "don't");
        checkBadCharacter("Bad character \" in start tag x\"", "x\"");
    }

    private void checkBadCharacter(String expectedMessage, String tag) {
        try {
            out.startTag(tag);
            fail();
        } catch (XmlWriterException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }
    
    public void testBadCharacterInSingleTag() throws Exception {
        try {
            out.singleTag("<html/>");
            fail();
        } catch (XmlWriterException e) {
            assertEquals("Bad character < in start tag <html/>", e.getMessage());
        }
    }

    public void testNamespaces() {
        /* As of now, the code is not at all namespace-aware.  Of course, it needs to be
         * namespace-tolerant (right now, that means treating namespaced attribute and
         * tag names just like any others). */
         out.startTag("html", "xmlns", "default/namespace");
         out.startTag("body", "xmlns:x", "some/other/namespace");
         out.singleTag("x:image", "x:width", "5");
         out.endTag("body");
         out.endTag("html");

         assertEquals("<html xmlns=\"default/namespace\">" +
            "<body xmlns:x=\"some/other/namespace\">" +
            "<x:image x:width=\"5\" />" +
            "</body>" +
            "</html>", out.getOutput());
    }
    
    public void testNewline() throws Exception {
        out.singleTag("html");
        out.newline();
        assertEquals("<html />\n", out.getOutput());
    }
    
    public void testIndent() throws Exception {
        out.startTag("html");
        out.newline();
        out.indent(4);
        out.singleTag("br");
        out.newline();
        out.endTag("html");
        out.newline();
        assertEquals(
            "<html>\n" +
            "    <br />\n" +
            "</html>\n",
            out.getOutput()
        );
    }
    
}
