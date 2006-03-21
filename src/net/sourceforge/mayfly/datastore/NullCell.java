package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;

public class NullCell extends Cell {
    
    public static final NullCell INSTANCE = new NullCell();
    
    private NullCell() {
    }

    public int asInt() {
        // JDBC seems to force this on us.
        return 0;
    }

    public long asLong() {
        // JDBC seems to force this on us.
        return 0;
    }

    public String asString() {
        // JDBC seems to force this on us (null, "", or what?).
        return null;
    }
    
    public String displayName() {
        return "null value";
    }

    public Object asObject() {
        // What are we supposed to return here?
        throw new MayflyException("Attempt to read SQL NULL as an object");
    }
    
    public Object asContents() {
        /**
         * This can probably go away if {@link net.sourceforge.mayfly.parser.Parser#parseAndEvaluate()}
         * returns a {@link Cell} rather than cell contents.
         */
        return NullCellContent.INSTANCE;
    }
    
    public int compareTo(Cell otherCell) {
        return otherCell instanceof NullCell ? 0 : -1;
    }

}
