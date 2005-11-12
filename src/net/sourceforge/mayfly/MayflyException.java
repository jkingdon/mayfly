package net.sourceforge.mayfly;
import java.sql.*;

/**
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
        sqlException = new SQLException(message);
    }

    public MayflyException(String message, Throwable cause) {
        super(message, cause);
        sqlException = (SQLException) new SQLException(message).initCause(cause);
    }

    public MayflyException(Throwable cause) {
        super(cause);
        sqlException = (SQLException) new SQLException(cause.getMessage()).initCause(cause);
    }

    public SQLException asSqlException() {
        return sqlException;
    }

}
