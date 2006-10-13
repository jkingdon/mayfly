package net.sourceforge.mayfly;

import net.sourceforge.mayfly.parser.Location;

import java.sql.SQLException;

/**
 * When Mayfly throws an SQLException, it will generally
 * be of this class, which has the ability to provide
 * information which is not standardized by the standard
 * SQLException.
 */
public class MayflySqlException extends SQLException {

    private final Location location;
    private final MayflyException runtimeException;

    MayflySqlException(MayflyException runtimeException) {
        super(runtimeException.getMessage());
        initCause(runtimeException.getCause());
        this.runtimeException = runtimeException;
        this.location = runtimeException.location;
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
     * Starting column of the SQL which caused the problem,
     * or -1 if the line number is not known.
     * One-based.
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

    /**
     * In many cases it may be useful to throw an SQLException
     * through code which is not declared <tt>throws SQLException</tt>.
     * 
     * The usual approach, which has the virtue of not being
     * Mayfly-specific, is to wrap the SQLException in a
     * RuntimeException:
     * <tt>&nbsp;&nbsp;&nbsp;&nbsp;throw new RuntimeException(sqlException);</tt>
     * That works well, although the stack traces are harder
     * to read because of the wrapping.
     * 
     * This method also provides a RuntimeException, but it
     * is not wrapped (it shows essentially the same stack
     * trace as the checked exception itself).
     */
    public RuntimeException asRuntimeException() {
        return runtimeException;
    }

}
