package net.sourceforge.mayfly;
import net.sourceforge.mayfly.parser.Location;

/**
 * When providing exceptions via Mayfly-specific methods,
 * mayfly will generally throw them as MayflyException.
 * This is a RuntimeException.  
 * If you prefer checked exceptions, call {@link #asSqlException()}.
 * 
 * @internal
 * For errors which we don't think will
 * make it out of mayfly itself, we can use this exception,
 * {@link MayflyInternalException},
 * RuntimeException, IllegalArgumentException, etc.
 * 
 * Otherwise, MayflyException turns into SQLException.
 */
public class MayflyException extends RuntimeException {
    
    private final MayflySqlException sqlException;

    /**
     * @internal
     * We keep track of where the error occurred.  It, of
     * course, is available to Java code which explicitly
     * checks for {@link MayflySqlException}.  But should
     * it be provided in another way as well?
     * Maybe the start line number
     * (not columns or end line number) in the message?  
     * Or put it in the message
     * only if it is not one (or only if the input is
     * more than one line)?
     * It isn't clear how to address both the desire
     * to be informative, but also the desire to
     * avoid clutter in the (many) cases in which the
     * line number won't be helpful.
     */
    protected final Location location;

    public MayflyException(String message) {
        this(message, Location.UNKNOWN);
    }

    public MayflyException(String message, Location location) {
        super(message);
        this.location = location;
        sqlException = new MayflySqlException(this);
    }

    public MayflyException(String message, Throwable cause) {
        super(message, cause);
        this.location = Location.UNKNOWN;
        sqlException = new MayflySqlException(this);
    }

    public MayflyException(Throwable cause) {
        super(cause);
        this.location = Location.UNKNOWN;
        sqlException = new MayflySqlException(this);
    }

    /**
     * Provide an exception which is similar to this one,
     * but which is a checked exception.  This
     * method and {@link MayflySqlException#asRuntimeException()}
     * can go back and forth between runtime and checked
     * exceptions without wrapping.
     */
    public MayflySqlException asSqlException() {
        return sqlException;
    }
    
    public Location location() {
        return location;
    }

    /**
     * Starting line number of the SQL which caused the problem,
     * or -1 if the location is not known.
     * An unknown location reflects a missing feature in Mayfly;
     * our eventual intention is to provide a location with all
     * exceptions.
     */
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

    /**
     * Ending line number of the SQL which caused the problem.
     * A value of -1 indicates that the end location is not
     * known.  It is possible for the start location to be
     * known and the end location not to be known, or vice
     * versa.
     */
    public int endLineNumber() {
        return location.endLineNumber;
    }

    /**
     * Ending column of the SQL which caused the problem.
     * For example, if the input SQL was " x " and the
     * text "x" was the problem, the start column would
     * be 2 and the end column would be 3.
     */
    public int endColumn() {
        return location.endColumn;
    }

}
