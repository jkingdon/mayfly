package net.sourceforge.mayfly;

/**
 * This is suitable for errors which we don't think will
 * make it out of mayfly itself.
 * 
 * We also might want to replace SQLException with MayflyException
 * internally, and unwrap them at the JDBC level (keeping
 * MayflyException at the Database level).  To be determined...
 */
public class MayflyException extends RuntimeException {

    public MayflyException(String message) {
        super(message);
    }

    public MayflyException(String message, Throwable cause) {
        super(message, cause);
    }

}
