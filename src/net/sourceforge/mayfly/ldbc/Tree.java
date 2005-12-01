package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

import org.apache.commons.lang.*;

import antlr.*;
import antlr.collections.*;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class Tree implements AST {


    public static Tree parse(String sql) throws SQLException {
        try {
            StringReader in = new StringReader(sql);
            SQLLexer lexer = new SQLLexer(in);
            SQLParser parser = new SQLParser(lexer);
            parser.statement();

            if (false) {
                DumpASTVisitor visitor2 = new DumpASTVisitor();
                visitor2.visit(parser.getAST());
            }
            return new Tree(parser.getAST());
        } catch (ANTLRException e) {
            // Do we want to report the text we were parsing, or line/column numbers?
            // Is there anything interesting about e other than its message (like its class?)
            throw new MayflyException(e.getMessage());
        }
    }

    private AST delegate;

    public Tree(AST delegate) {
        this.delegate = delegate;
    }

    public boolean equals(Object obj) {
        return doEquals(obj);
    }

    public boolean equals(AST ast) {
        return doEquals(ast);
    }

    private boolean doEquals(Object obj) {
        Tree other = (Tree) obj;

        return getText().equals(other.getText()) &&
               getType()==other.getType() &&
               children().equals(other.children());
    }



    public int hashCode() {
        return 0;
    }

    public void print() {
        System.out.println(toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        treeString("", this, sb);
        return sb.toString();
    }


    private void treeString(String prefix, AST ast, StringBuilder sb) {
        sb.append(prefix + ast.getText() + " (" + typeName(ast.getType()) + ")" + "\n");

        AST child = ast.getFirstChild();
        if (child!=null) {
            treeString(prefix + " |-> ", child, sb);
        }

        AST nextSibling = ast.getNextSibling();
        if (nextSibling!=null) {
            treeString(prefix, nextSibling, sb);
        }
    }

    private String typeName(int code) {

        try {
            SQLTokenTypes t = new SQLTokenTypes() {};

            for (int i = 0; i < SQLTokenTypes.class.getFields().length; i++) {
                Field f = SQLTokenTypes.class.getFields()[i];
                if (f.get(t).equals(new Integer(code))) {
                    return f.getName();
                }
            }
            throw new RuntimeException("not found");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    public void addChild(AST ast) {
        delegate.addChild(ast);
    }



    public boolean equalsList(AST ast) {
        return delegate.equalsList(ast);
    }

    public boolean equalsListPartial(AST ast) {
        return delegate.equalsListPartial(ast);
    }

    public boolean equalsTree(AST ast) {
        return delegate.equalsTree(ast);
    }

    public boolean equalsTreePartial(AST ast) {
        return delegate.equalsTreePartial(ast);
    }

    public ASTEnumeration findAll(AST ast) {
        return delegate.findAll(ast);
    }

    public ASTEnumeration findAllPartial(AST ast) {
        return delegate.findAllPartial(ast);
    }

    public AST getFirstChild() {
        return delegate.getFirstChild();
    }

    public AST getNextSibling() {
        return delegate.getNextSibling();
    }

    public String getText() {
        return delegate.getText();
    }

    public int getType() {
        return delegate.getType();
    }

    public void initialize(int i, String s) {
        delegate.initialize(i, s);
    }

    public void initialize(AST ast) {
        delegate.initialize(ast);
    }

    public void initialize(Token token) {
        delegate.initialize(token);
    }

    public void setFirstChild(AST ast) {
        delegate.setFirstChild(ast);
    }

    public void setNextSibling(AST ast) {
        delegate.setNextSibling(ast);
    }

    public void setText(String s) {
        delegate.setText(s);
    }

    public void setType(int i) {
        delegate.setType(i);
    }

    public String toStringList() {
        return delegate.toStringList();
    }

    public String toStringTree() {
        return delegate.toStringTree();
    }

    public int getColumn() {
        return delegate.getColumn();
    }

    public int getLine() {
        return delegate.getLine();
    }

    public int getNumberOfChildren() {
        return delegate.getNumberOfChildren();
    }

    public Children children() {
        Collection elements = new ArrayList();

        if (getFirstChild()!=null) {
            Tree child = new Tree(getFirstChild());
            elements.add(child);

            while (child.getNextSibling()!=null) {
                Tree nextSibling = new Tree(child.getNextSibling());
                elements.add(nextSibling);

                child = nextSibling;
            }
        }
        return new Children(elements);
    }

    public static class Children extends Aggregate {
        private Collection elements;

        public Children(Collection elements) {
            this.elements = elements;
        }

        protected Aggregate createNew(Iterable items) {
            return new Tree.Children(new L().addAll(items));
        }

        public Iterator iterator() {
            return elements.iterator();
        }

        public Children ofType(int type) {
            return ofTypes(new int[] {type});
        }

        public Children ofTypes(int[] types) {
            return (Children) select(new TypeIsAnyOf(types));
        }

        public Tree singleSubtreeOfType(int type) {
            return (Tree)find(new TypeIs(type));
        }

        public L convertUsing(final TreeConverters converters) {
            return convertUsing(converters, new int[0]);
        }

        public L convertUsing(final TreeConverters converters, int[] typesToIgnore) {
            Children selected = (Children) select(new AllExceptTypes(typesToIgnore));

            final L results = new L();
            
            for (int i = 0; i < selected.size(); i++) {
                int lookaheadIndex = i + 1;
                Tree tree = (Tree) selected.element(i);

                if (lookaheadIndex < selected.size()) {
                    Tree lookahead = (Tree) selected.element(lookaheadIndex);
                    if (lookahead.getType() == SQLTokenTypes.JOIN) {
                        FromElement left = (FromElement) new Convert(converters).transform(tree);
                        Children joinChildren = lookahead.children();
                        Tree type = (Tree) joinChildren.element(0);

                        Tree rightTree = (Tree) joinChildren.element(1);
                        FromElement right = (FromElement) new Convert(converters).transform(rightTree);
                        Tree conditionTree = (Tree) joinChildren.element(2);
                        Where condition = (Where) new Convert(converters).transform(conditionTree);
                        
                        if (type.getType() == SQLTokenTypes.LITERAL_inner) {
                            results.add(new InnerJoin(left, right, condition));
                        } else if (type.getType() == SQLTokenTypes.LITERAL_left) {
                            results.add(new LeftJoin(left, right, condition));
                        }
                        i += 1;
                        continue;
                    }

                }
                
                results.add(new Convert(converters).transform(tree));
            }
            
            return results;
        }
    }

    public static class TypeIs implements Selector {
        private int type;

        public TypeIs(int type) {
            this.type = type;
        }

        public boolean evaluate(Object candidate) {
            return ((Tree)candidate).getType() == type;
        }
    }

    public static class TypeIsAnyOf implements Selector {
        private List possibleTypes;

        public TypeIsAnyOf(int[] possibleTypes) {
            this.possibleTypes = Arrays.asList(ArrayUtils.toObject(possibleTypes));
        }

        public boolean evaluate(Object candidate) {
            return possibleTypes.contains(new Integer(((Tree)candidate).getType()));
        }
    }

    public static class AllExceptTypes implements Selector {
        private L typesToIgnore;

        public AllExceptTypes(int[] typesToIgnore) {
            this.typesToIgnore = L.fromArray(typesToIgnore);
        }

        public boolean evaluate(Object candidate) {
            Tree t = (Tree) candidate;
            return !typesToIgnore.contains(t.getType());
        }
    }

    public static class Convert implements Transformer {
        private TreeConverters converters;

        public Convert(TreeConverters converters) {
            this.converters = converters;
        }

        public Object transform(Object from) {
            Tree t = (Tree) from;
            return converters.transform(t);
        }
    }
}
