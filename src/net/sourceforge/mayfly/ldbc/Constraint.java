package net.sourceforge.mayfly.ldbc;

abstract public class Constraint extends ValueObject {
    public static Constraint fromTree(Tree tree) {
        return null;
    }


    public static class Equal extends Constraint {
        public static Constraint fromTree(Tree tree) {
//            Iterator iter = tree.children().iterator();
//
//            AST left = (AST) iter.next();
//            AST right = (AST) iter.next();
            return null;
        }

        private Object leftside;
        private Object rightside;

        public Equal(Object leftside, Object rightside) {
            this.leftside = leftside;
            this.rightside = rightside;
        }
    }
}
