package net.sourceforge.mayfly;
import net.sourceforge.mayfly.parser.Location;

import java.sql.SQLException;

/**
 * @internal
 * For errors which we don't think will
 * make it out of mayfly itself, we can use this exception, or
 * RuntimeException, or IllegalArgumentException (etc).
 * 
 * Otherwise, MayflyException turns into SQLException.
 */
public class MayflyException extends RuntimeException {
    
    private final SQLException sqlException;

    public MayflyException(String message) {
        this(message, Location.UNKNOWN);
    }

    public MayflyException(String message, Location location) {
        super(message);
        sqlException = new MayflySqlException(message, location);
    }

    public MayflyException(String message, Throwable cause) {
        super(message, cause);
        sqlException = new MayflySqlException(message, cause, Location.UNKNOWN);
    }

    public MayflyException(Throwable cause) {
        super(cause);
        sqlException = new MayflySqlException(cause, Location.UNKNOWN);
    }

    public SQLException asSqlException() {
        return sqlException;
    }

}
