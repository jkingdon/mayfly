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
    private final int startLineNumber;
    private final int startColumn;
    private final int endLineNumber;
    private final int endColumn;

    public ParserException(String message, 
        int startLineNumber, int startColumn, 
        int endLineNumber, int endColumn) {
        super(message);
        this.startLineNumber = startLineNumber;
        this.startColumn = startColumn;
        this.endLineNumber = endLineNumber;
        this.endColumn = endColumn;
    }

    public ParserException(String message) {
        this(message, -1, -1, -1, -1);
    }
    
    public ParserException(String expected, Token actual) {
        this("expected " +
            expected +
            " but got " +
            actual.describe(),
            actual.startLineNumber(),
            actual.startColumn(),
            actual.endLineNumber(),
            actual.endColumn());
    }

    public int startLineNumber() {
        return startLineNumber;
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
        return startColumn;
    }

    public int endLineNumber() {
        return endLineNumber;
    }

    public int endColumn() {
        return endColumn;
    }

}
