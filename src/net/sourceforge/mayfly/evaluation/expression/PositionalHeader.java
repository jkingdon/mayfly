package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.CellHeader;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;

public class PositionalHeader implements CellHeader {

    private final int positionInSelected;
    public final Expression expression;

    /**
     * @internal
     * @param positionInSelected Position in the 
     * {@link net.sourceforge.mayfly.ldbc.what.Selected}.  Note that from the
     * point of view of a {@link Row}, this is just a magic cookie, so
     * the word position is a potentially confusing way to refer to it.
     */
    public PositionalHeader(int positionInSelected) {
        this(positionInSelected, null);
    }
    
    public PositionalHeader(int positionInSelected, Expression expression) {
        this.positionInSelected = positionInSelected;
        this.expression = expression;
    }
    
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (!(obj.getClass() == PositionalHeader.class)) { return false; }
        return positionInSelected == ((PositionalHeader)obj).positionInSelected;
    }
    
    public int hashCode() {
        return positionInSelected;
    }
    
    public String toString() {
        return "positional header #" + positionInSelected;
    }

}
