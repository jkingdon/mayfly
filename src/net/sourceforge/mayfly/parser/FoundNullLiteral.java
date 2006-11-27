package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;

public class FoundNullLiteral extends MayflyException {

    public FoundNullLiteral(Location location) {
        super("To check for null, use IS NULL or IS NOT NULL, not a null literal",
            location);
    }

}
