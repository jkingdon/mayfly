package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.ldbc.rowmask.*;
import org.ldbc.parser.*;

import java.util.*;

public class TreeConverters {

    public static TreeConverters forSelectTree() {
        return new TreeConverters()
            .register(SQLTokenTypes.SELECT,         new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return Select.fromTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.TABLE_ASTERISK, new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return WholeDimension.fromTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.SELECT_ITEM,    new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return SingleColumnExpression.fromExpressionTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.ASTERISK,       new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return new Everything();
                                                          }
                                                      })
            .register(SQLTokenTypes.COLUMN,        new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return Column.fromColumnTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.SELECTED_TABLE,new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return Dimension.fromSeletedTableTree(from);
                                                          }
                                                      });
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
}
