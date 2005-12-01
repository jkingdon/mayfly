package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

import java.util.*;

public class TreeConverters {

    public static TreeConverters forSelectTree() {
        return new TreeConverters()
            .register(SQLTokenTypes.SELECT,         new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return Select.selectFromTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.TABLE_ASTERISK, new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return AllColumnsFromTable.fromTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.SELECT_ITEM,    new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return WhatElement.fromSelectItemTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.ASTERISK,       new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return new All();
                                                          }
                                                      })
            .register(SQLTokenTypes.SELECTED_TABLE,new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return FromTable.fromSeletedTableTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.JOIN,      new TreeConverters.Converter() {
                public Object convert(Tree from, TreeConverters converters) {
                    return Join.fromJoinTree(from, converters);
                }
            })
            .register(SQLTokenTypes.CONDITION,     new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return Where.fromConditionTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.ORDER_BY,      new TreeConverters.Converter() {
                public Object convert(Tree from, TreeConverters converters) {
                    return OrderBy.fromTree(from);
                }
            })
            .register(SQLTokenTypes.LIMIT,      new TreeConverters.Converter() {
                public Object convert(Tree from, TreeConverters converters) {
                    return Limit.fromTree(from);
                }
            })
            ;
    }

    public static TreeConverters forWhereTree() {
        return new TreeConverters()
            .register(SQLTokenTypes.LITERAL_and,   new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return And.fromAndTree(from, converters);
                                                          }
                                                      })
            .register(SQLTokenTypes.LITERAL_or,   new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return Or.fromOrTree(from, converters);
                                                          }
                                                      })
            .register(SQLTokenTypes.NOT,   new TreeConverters.Converter() {
                public Object convert(Tree from, TreeConverters converters) {
                    return Not.fromNotTree(from, converters);
                }
            })
            .register(SQLTokenTypes.LITERAL_in,   new TreeConverters.Converter() {
                public Object convert(Tree from, TreeConverters converters) {
                    return In.fromInTree(from, converters);
                }
            })
            .register(SQLTokenTypes.EQUAL,         new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return Eq.fromEqualTree(from, converters);
                                                          }
                                                      })
            .register(SQLTokenTypes.NOT_EQUAL,     new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return NotEq.fromNotEqualTree(from, converters);
                                                          }
                                                      })
            .register(SQLTokenTypes.NOT_EQUAL_2,   new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return NotEq.fromNotEqualTree(from, converters);
                                                          }
                                                      })
            .register(SQLTokenTypes.BIGGER,   new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return Gt.fromBiggerTree(from, converters);
                                                          }
                                                      })
            .register(SQLTokenTypes.SMALLER,   new TreeConverters.Converter() {
                public Object convert(Tree from, TreeConverters converters) {
                    return Gt.fromSmallerTree(from, converters);
                }
            })
            .register(SQLTokenTypes.IS_NULL,         new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return IsNull.fromIsNullTree(from, converters);
                                                          }
                                                      })
            .register(SQLTokenTypes.IS_NOT_NULL,     new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return IsNull.fromIsNotNullTree(from, converters);
                                                          }
                                                      })
            .register(SQLTokenTypes.COLUMN,        new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return WhatElement.fromExpressionTree(from);
                                                          }
                                                      })
            .register(SQLTokenTypes.PARAMETER,       new TreeConverters.Converter() {
                                                          public Object convert(Tree from, TreeConverters converters) {
                                                              return JdbcParameter.INSTANCE;
                                                          }
                                                      })
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

    public static TreeConverters forOrderBy() {
        return new TreeConverters()
            .register(SQLTokenTypes.ORDER_ITEM,   new TreeConverters.Converter() {
                    public Object convert(Tree from, TreeConverters converters) {
                        return OrderItem.fromTree(from, converters);
                    }
                })
            .register(SQLTokenTypes.COLUMN,        new TreeConverters.Converter() {
                    public Object convert(Tree from, TreeConverters converters) {
                        return WhatElement.fromExpressionTree(from);
                    }
                })
            ;
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
