package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;

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
