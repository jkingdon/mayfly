package net.sourceforge.mayfly.datastore;

public class NullCell extends Cell {
    
    public static final NullCell INSTANCE = new NullCell();
    
    private NullCell() {
        super(NullCellContent.INSTANCE);
    }

}
