package net.sourceforge.mayfly.ldbc;

import org.ldbc.parser.*;

public class Select extends ValueObject {

    public static Select fromTree(Tree selectTree) {
        return
            new Select(
                Dimensions.fromTableTrees(selectTree.children().ofType(SQLTokenTypes.SELECTED_TABLE))
            );
    }

    
    private Dimensions from;

    public Select(Dimensions from) {
        this.from = from;
    }

    public Dimensions from() {
        return from;
    }
    
}
