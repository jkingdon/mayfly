package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.ldbc.rowmask.*;
import net.sourceforge.mayfly.util.*;
import org.ldbc.parser.*;

public class Select extends ValueObject {

    public static Select fromTree(Tree selectTree) {

        int[] typesToIgnore = new int[]{SQLTokenTypes.COMMA};

        L converted =
            selectTree.children().convertUsing(TreeConverters.forSelectTree(), typesToIgnore);

        return
            new Select(
                new RowMask(converted.selectObjectsThatAre(RowMaskElement.class)),
                new Dimensions(converted.selectObjectsThatAre(Dimension.class))
            );
    }


    private RowMask what;
    private Dimensions from;

    public Select(RowMask what, Dimensions from) {
        this.what = what;
        this.from = from;
    }

    public Dimensions from() {
        return from;
    }

}
