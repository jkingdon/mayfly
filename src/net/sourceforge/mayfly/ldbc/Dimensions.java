package net.sourceforge.mayfly.ldbc;

import org.ldbc.antlr.collections.*;

import java.util.*;
import java.util.List;

public class Dimensions extends Enumerable<Dimensions, Dimension> {

    private Collection<Dimension> dimensions = new ArrayList<Dimension>();


    public Dimensions(Dimension... dimensions) {
        this(Arrays.asList(dimensions));
    }

    private Dimensions(Collection<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    protected Dimensions createNew(Collection<Dimension> items) {
        return new Dimensions(items);
    }

    public Iterator<Dimension> iterator() {
        return dimensions.iterator();
    }

    public static Dimensions fromTableTrees(Iterable<Tree> tables) {
        List<Dimension> elements = new ArrayList<Dimension>();

        for (Tree table : tables) {
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
}
