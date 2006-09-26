package net.sourceforge.mayfly;
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
    
    private SQLException sqlException;

    public MayflyException(String message) {
        super(message);
        sqlException = new MayflySqlException(message);
    }

    public MayflyException(String message, Throwable cause) {
        super(message, cause);
        sqlException = new MayflySqlException(message, cause);
    }

    public MayflyException(Throwable cause) {
        super(cause);
        sqlException = new MayflySqlException(cause);
    }

    public SQLException asSqlException() {
        return sqlException;
    }

}
