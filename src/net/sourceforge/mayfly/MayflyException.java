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
        sqlException = new MayflySqlException(message, location);
        this.location = location;
    }

    public MayflyException(String message, Throwable cause) {
        super(message, cause);
        Location location = Location.UNKNOWN;
        sqlException = new MayflySqlException(message, cause, location);
        this.location = location;
    }

    public MayflyException(Throwable cause) {
        super(cause);
        Location location = Location.UNKNOWN;
        sqlException = new MayflySqlException(cause, location);
        this.location = location;
    }

    public MayflySqlException asSqlException() {
        return sqlException;
    }
    
    public Location location() {
        return location;
    }

}
