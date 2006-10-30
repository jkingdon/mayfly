package net.sourceforge.mayfly;

import net.sourceforge.mayfly.parser.Location;

/**
 * This exception indicates that Mayfly does not yet implement
 * a feature which you are trying to use.  We suggest reporting
 * your experience to the Mayfly mailing lists - we don't know
 * what features to work on unless people tell us which ones they
 * would find most useful.
 */
public class UnimplementedException extends MayflyException {

    private static final String DEFAULT_MESSAGE = 
        "This feature is not yet implemented in Mayfly";

    public UnimplementedException() {
        super(DEFAULT_MESSAGE);
    }

    public UnimplementedException(Location location) {
        super(DEFAULT_MESSAGE, location);
    }

    public UnimplementedException(String message) {
        super(message);
    }

    public UnimplementedException(String message, Location location) {
        super(message, location);
    }

}
