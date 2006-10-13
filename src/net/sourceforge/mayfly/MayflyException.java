package net.sourceforge.mayfly;
import net.sourceforge.mayfly.parser.Location;

/**
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

    public MayflySqlException asSqlException() {
        return sqlException;
    }
    
    public Location location() {
        return location;
    }

}
