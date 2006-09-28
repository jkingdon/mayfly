package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;

public class ParserException extends MayflyException {

    public ParserException(String message, Location location) {
        super(message, location);
    }

    public ParserException(String message) {
        this(message, Location.UNKNOWN);
    }
    
    public ParserException(String expected, Token actual) {
        this("expected " +
            expected +
            " but got " +
            actual.describe(),
            actual.location);
    }

    public int startLineNumber() {
        return location.startLineNumber;
    }

    /**
     * The column of the start of the text being
     * flagged.  This is 1-based.  For example,
     * if the whole line is "foo bar baz" and
     * the error is on "bar", this method will
     * return 5 and {@link #endColumn()} will
     * return 8.
     */
    public int startColumn() {
        return location.startColumn;
    }

    public int endLineNumber() {
        return location.endLineNumber;
    }

    public int endColumn() {
        return location.endColumn;
    }

}
