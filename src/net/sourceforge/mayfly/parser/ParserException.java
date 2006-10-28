package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;

public class ParserException extends MayflyException {

    public ParserException(String message, Location location) {
        super(message, location);
    }

    public ParserException(String message) {
        this(message, Location.UNKNOWN);
    }
    
    public ParserException(String expected, Token actual) {
        this("expected " +
            expected +
            " but got " +
            actual.describe(),
            actual.location);
    }

}
