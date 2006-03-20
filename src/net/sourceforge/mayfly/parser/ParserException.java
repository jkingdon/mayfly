package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;

public class ParserException extends MayflyException {

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }

}
