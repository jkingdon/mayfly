package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public class PositionalHeader extends ValueObject implements CellHeader {

    private final int positionInSelected;

    /**
     * @internal
     * @param positionInSelected Position in the selected form of the
     * {@link net.sourceforge.mayfly.ldbc.what.What}.  Note that from the
     * point of view of a {@link Row}, this is just a magic cookie, so
     * the word position is a potentially confusing way to refer to it.
     */
    public PositionalHeader(int positionInSelected) {
        this.positionInSelected = positionInSelected;
    }
    
    public String toString() {
        return "positional header #" + positionInSelected;
    }

}
