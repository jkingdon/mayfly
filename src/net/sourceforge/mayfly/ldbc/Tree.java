package net.sourceforge.mayfly.ldbc;

import org.ldbc.antlr.*;
import org.ldbc.antlr.collections.*;
import org.ldbc.core.*;
import org.ldbc.parser.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class Tree implements AST {


    public static Tree parse(String sql) {
        try {
            StringReader in = new StringReader(sql);
            SQLLexer lexer = new SQLLexer(in);
            SQLParser parser = new SQLParser(lexer);
            parser.statement();

            if (Trace.isDetailed()) {
                DumpASTVisitor visitor2 = new DumpASTVisitor();
                visitor2.visit(parser.getAST());
            }
            return new Tree(parser.getAST());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AST delegate;

    protected Tree(AST delegate) {
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

            for (Field f : SQLTokenTypes.class.getFields()) {
                if (f.get(t).equals(code)) {
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

    public Children children() {
        Collection<Tree> elements = new ArrayList<Tree>();

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

    public static class Children extends Enumerable<Children, Tree> {
        private Collection<Tree> elements;

        public Children(Collection<Tree> elements) {
            this.elements = elements;
        }

        protected Children createNew(Collection<Tree> items) {
            return new Tree.Children(items);
        }

        public Iterator<Tree> iterator() {
            return elements.iterator();
        }

    }
}
