package net.sourceforge.mayfly.dump;

import net.sourceforge.mayfly.ldbc.*;

import java.util.Stack;

public class XmlWriter {
    
    // TODO: For mayfly, probably want a streaming version of this.

    private StringBuilder out = new StringBuilder();
    private Stack openElements = new Stack();

    public void startTag(String tag) {
        startTag(tag, new String[] {});
    }

    public void startTag(String tag, String attributeName, String attributeValue) {
        startTag(tag, new String[] {attributeName, attributeValue});
    }

    public void startTag(String tag, String[] attributes) {
        out.append("<");
        tagName(tag);

        attributes(attributes);

        out.append(">");

        openElements.push(tag);
    }

    private void tagName(String tag) {
        rejectCharacter(tag, '<');
        rejectCharacter(tag, '&');
        rejectCharacter(tag, '>');
        rejectCharacter(tag, '\'');
        rejectCharacter(tag, '"');
        out.append(tag);
    }

    private void rejectCharacter(String tag, char character) {
        if (tag.indexOf(character) != -1) {
            throw new XmlWriterException("Bad character " + character + " in start tag " + tag);
        }
    }

    private void attribute(String attributeName, String attributeValue) {
        out.append(" ");
        out.append(attributeName);
        out.append("=\"");
        out.append(attributeValue.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;"));
        out.append("\"");
    }

    private void attributes(String[] attributes) {
        for (int i = 0; i < attributes.length; i += 2) {
            attribute(attributes[i], attributes[i + 1]);
        }
    }

    public void endTag(String tag) {
        Object startTag = openElements.pop();
        if (!tag.equals(startTag)) {
            throw new XmlWriterException(
                "end tag " + tag + " does not match start tag " + startTag);
        }

        out.append("</");
        out.append(tag);
        out.append(">");
    }

    public void singleTag(String tag, String[] attributes) {
        out.append("<");
        tagName(tag);
        attributes(attributes);
        out.append(" />");
    }
    
    public void singleTag(String tag) {
        singleTag(tag, new String[] {});
    }

    public void singleTag(String tag, String attributeName, String attributeValue) {
        singleTag(tag, new String[] {attributeName, attributeValue});
    }

    public String getOutput() {
        if (!openElements.isEmpty()) {
            throw new XmlWriterException("unclosed element " + openElements.peek());
        }
        return out.toString();
    }

    public void text(String text) {
        out.append(text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
    }

    public void newline() {
        out.append('\n');
    }

    public void indent(int spaces) {
        for (int i = 0; i < spaces; ++i) {
            out.append(' ');
        }
    }

}
