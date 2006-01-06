package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

import java.util.*;

public class TreeConverters {

    public static TreeConverters forWhereTree() {
        return new TreeConverters()
            .register(SQLTokenTypes.QUOTED_STRING, new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return QuotedString.fromQuotedStringTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.DECIMAL_VALUE, new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return MathematicalInt.fromDecimalValueTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.LITERAL_null,  new TreeConverters.Converter() {
                public Object convert(Tree from, TreeConverters converters) {
                    throw new MayflyException("To check for null, use IS NULL or IS NOT NULL, not a null literal");
                }
            })
            .register(SQLTokenTypes.OPEN_PAREN,    new TreeConverters.SkipLevelAndContinue())
            .register(SQLTokenTypes.CONDITION,    new TreeConverters.SkipLevelAndContinue());
    }

    
    private Map typeToConverter = new HashMap();

    public TreeConverters register(int type, Converter treeConverter) {
        typeToConverter.put(new Integer(type), treeConverter);
        return this;
    }

    public boolean canTransform(Tree tree) {
        return typeToConverter.containsKey(new Integer(tree.getType()));
    }

    public Object transform(Tree tree) {
        if (!canTransform(tree)) {
            throw new RuntimeException("can't transform: \n" + tree.toString());
        }

        return ((Converter)typeToConverter.get(new Integer(tree.getType()))).convert(tree, this);
    }



    public static interface Converter {
        Object convert(Tree from, TreeConverters converters);
    }

    public static class SkipLevelAndContinue implements Converter {
        public Object convert(Tree from, TreeConverters converters) {
            return converters.transform(new Tree(from.getFirstChild()));
        }
    }
}
