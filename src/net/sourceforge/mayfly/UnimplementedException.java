package net.sourceforge.mayfly;

/**
 * This exception indicates that Mayfly does not yet implement
 * a feature which you are trying to use.  We suggest reporting
 * your experience to the Mayfly mailing lists - we don't know
 * what features to work on unless people tell us which ones they
 * would find most useful.
 */
public class UnimplementedException extends MayflyException {

    public UnimplementedException() {
        super("This feature is not yet implemented in Mayfly");
    }

    public UnimplementedException(String message) {
        super(message);
    }

    public UnimplementedException(Throwable cause) {
        super(cause);
    }

    public UnimplementedException(String message, Throwable cause) {
        super(message, cause);
    }

}
