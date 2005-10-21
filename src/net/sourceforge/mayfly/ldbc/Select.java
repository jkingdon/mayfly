package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;
import org.ldbc.parser.*;

public class Select extends ValueObject {

    public static Select fromTree(Tree selectTree) {

        int[] typesToIgnore = new int[]{SQLTokenTypes.COMMA};

        L converted =
            selectTree.children().convertUsing(TreeConverters.forSelectTree(), typesToIgnore);

        return
            new Select(
                new What(converted.selectObjectsThatAre(WhatElement.class)),
                new Froms(converted.selectObjectsThatAre(From.class)),
                (Where) converted.selectObjectThatIs(Where.class)
            );
    }


    private What what;
    private Froms from;

    public Select(What what, Froms from, Where where) {
        this.what = what;
        this.from = from;
    }

    public Froms from() {
        return from;
    }

}
