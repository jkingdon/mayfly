package net.sourceforge.mayfly;
import java.sql.*;

/**
 * This is suitable for errors which we don't think will
 * make it out of mayfly itself.
 * 
 * We also might want to replace SQLException with MayflyException
 * internally, and only unwrap them at the JDBC level (keeping
 * MayflyException at the Database level).  To be determined...
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
        sqlException = (SQLException) new SQLException().initCause(cause);
    }

    public SQLException asSqlException() {
        // This wrapping isn't really desirable.  It can be avoided by
        // having a MayflyException create an SQLException inside it.
        return sqlException;
    }

}
