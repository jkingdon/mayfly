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

    public MayflySqlException(String message, Location location) {
        super(message);
        this.location = location;
    }

    public MayflySqlException(String message, Throwable cause, Location location) {
        super(message);
        initCause(cause);
        this.location = location;
    }

    public MayflySqlException(Throwable cause, Location location) {
        super(cause.getMessage());
        initCause(cause);
        this.location = location;
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

}
