package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;

public class ParserException extends MayflyException {

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }

    public int startLineNumber() {
        throw new UnimplementedException();
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
        throw new UnimplementedException();
    }

    public int endLineNumber() {
        throw new UnimplementedException();
    }

    public int endColumn() {
        throw new UnimplementedException();
    }

}
