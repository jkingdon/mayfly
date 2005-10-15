package net.sourceforge.mayfly.ldbc;

import org.ldbc.antlr.collections.*;

import java.util.*;
import java.util.List;

public class Dimensions extends Enumerable {

    private Collection dimensions = new ArrayList();


    public Dimensions() { }

    private Dimensions(Collection dimensions) {
        this.dimensions = dimensions;
    }


    protected Object createNew(Iterable items) {
        return new Dimensions(asList(items));
    }

    public Iterator iterator() {
        return dimensions.iterator();
    }

    public static Dimensions fromTableTrees(Iterable tables) {
        List elements = new ArrayList();

        for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
            Tree table = (Tree) iterator.next();

            AST firstIdentifier = table.getFirstChild();
            String tableName = firstIdentifier.getText();

            AST secondIdentifier = firstIdentifier.getNextSibling();

            if (secondIdentifier==null) {
                elements.add(new Dimension(tableName));
            } else {
                String alias = secondIdentifier.getText();
                elements.add(new Dimension(tableName, alias));
            }
        }

        return new Dimensions(elements);
    }

    public Dimensions add(Dimension dimension) {
        dimensions.add(dimension);
        return this;
    }
}
