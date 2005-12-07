package net.sourceforge.mayfly;

/**
 * This exception indicates a bug in mayfly.  Although it is possible
 * that you can work around it by changing your code, or the exception
 * indicates that Mayfly is reporting a legitimate problem in an
 * unclear way, still the fact you are getting this exception means
 * there is a problem in Mayfly.  We'd encourage you to report it
 * to the Mayfly mailing lists.
 */
public class MayflyInternalException extends RuntimeException {

    public MayflyInternalException() {
        super();
    }

    public MayflyInternalException(String message) {
        super(message);
    }

    public MayflyInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public MayflyInternalException(Throwable cause) {
        super(cause);
    }

}
