package net.sourceforge.mayfly;

import net.sourceforge.mayfly.parser.Location;

/**
 * This exception indicates a bug in mayfly.  Although it is possible
 * that you can work around it by changing your code, or the exception
 * indicates that Mayfly is reporting a legitimate problem in an
 * unclear way, still the fact you are getting this exception means
 * there is a problem in Mayfly.  We'd encourage you to report it
 * to the Mayfly mailing lists.
 * 
 * @internal
 * This inherits from {@link MayflyException} because it might have
 * a location.  Is that a good enough reason?  Will it encourage
 * the old habits of thinking an "internal" exception is really 
 * a problem in their own code? (since it gets caught by the
 * same catch which might catch real exceptions, at least when
 * calling methods like {@link Database#execute(String)}).
 */
public class MayflyInternalException extends MayflyException {

    public MayflyInternalException(String message) {
        super(message);
    }

    public MayflyInternalException(String message, Location location) {
        super(message, location);
    }

    public MayflyInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public MayflyInternalException(Throwable cause) {
        super(cause);
    }

}
