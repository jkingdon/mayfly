package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.*;

public class FoundNullLiteral extends MayflyException {

    public FoundNullLiteral() {
        super("To check for null, use IS NULL or IS NOT NULL, not a null literal");
    }

}
