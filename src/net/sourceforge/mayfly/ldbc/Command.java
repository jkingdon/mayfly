package net.sourceforge.mayfly.ldbc;

import org.ldbc.parser.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.util.*;

public class Command extends ValueObject {

    public static Command fromTree(Tree tree) {
        switch (tree.getType()) {
        case SQLTokenTypes.DROP_TABLE:
            return DropTable.dropTableFromTree(tree);
        case SQLTokenTypes.CREATE_TABLE:
            return CreateTable.createTableFromTree(tree);
        case SQLTokenTypes.INSERT:
            return Insert.insertFromTree(tree);
        default:
            throw new UnimplementedException("Unrecognized command " + tree);
        }
    }

}
