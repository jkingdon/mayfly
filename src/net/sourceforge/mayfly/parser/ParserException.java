package net.sourceforge.mayfly.parser;

import java.sql.SQLException;

import net.sourceforge.mayfly.MayflyException;

public class ParserException extends MayflyException {

    /**
     * We keep track of where the error occurred.  It, of
     * course, is available to Java code which has the
     * {@link ParserException} (not yet carried over
     * to the {@link SQLException}).  But how else
     * should it be provided?  Maybe the start line number
     * (not columns or end line number) in the message?  
     * Or put it in the message
     * only if it is not one (or only if the input is
     * more than one line)?
     * It isn't clear how to address both the desire
     * to be informative, but also the desire to
     * avoid clutter in the (many) cases in which the
     * line number won't be helpful.
     */
    public final Location location;

    public ParserException(String message, Location location) {
        super(message);
        this.location = location;
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
