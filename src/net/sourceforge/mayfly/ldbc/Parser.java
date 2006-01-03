package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.parser.*;
import antlr.*;

import java.io.*;
import java.util.*;

/** @internal
 * This is an experimental attempt at a hand-written recursive
 * descent parser.
 */
public class Parser {

    private static List getAllTokens(String sql) throws ANTLRException {
        StringReader in = new StringReader(sql);
        SQLLexer lexer = new SQLLexer(in);
        List tokens = new ArrayList();
        while (true) {
            Token token = lexer.nextToken();
            tokens.add(token);
            if (token.getType() == SQLTokenTypes.EOF) {
                return tokens;
            }
        }
    }

    private List tokens;

    public Parser(String sql) throws ANTLRException {
        this(getAllTokens(sql));
    }

    public Parser(List tokens) {
        this.tokens = tokens;
    }

    public void parseSelect() {
        expectAndConsume(SQLTokenTypes.LITERAL_select);
        parseWhat();
        expectAndConsume(SQLTokenTypes.LITERAL_from);
        parseFromItems();
        
        if (currentTokenType() == SQLTokenTypes.LITERAL_where) {
            expectAndConsume(SQLTokenTypes.LITERAL_where);
            parseCondition();
        }
        
        expectAndConsume(SQLTokenTypes.EOF);
    }

    void parseWhat() {
        parseWhatElement();
        
        while (currentTokenType() == SQLTokenTypes.COMMA) {
            expectAndConsume(SQLTokenTypes.COMMA);
            parseWhatElement();
        }
    }

    private void parseWhatElement() {
        if (currentTokenType() == SQLTokenTypes.ASTERISK) {
            expectAndConsume(SQLTokenTypes.ASTERISK);
        } else if (currentTokenType() == SQLTokenTypes.IDENTIFIER) {
            parseColumnReference();
        }
    }

    void parseCondition() {
        parsePrimary();
        expectAndConsume(SQLTokenTypes.EQUAL);
        parsePrimary();
    }

    private void parsePrimary() {
        if (currentTokenType() == SQLTokenTypes.IDENTIFIER) {
            parseColumnReference();
        } else if (currentTokenType() == SQLTokenTypes.DECIMAL_VALUE) {
            expectAndConsume(SQLTokenTypes.DECIMAL_VALUE);
        }
    }

    private void parseColumnReference() {
        expectAndConsume(SQLTokenTypes.IDENTIFIER);
        if (currentTokenType() == SQLTokenTypes.DOT) {
            expectAndConsume(SQLTokenTypes.DOT);
            expectAndConsume(SQLTokenTypes.IDENTIFIER);
        }
    }

    void parseFromItems() {
        parseFromItem();
        
        while (currentTokenType() == SQLTokenTypes.COMMA) {
            expectAndConsume(SQLTokenTypes.COMMA);
            parseFromItem();
        }
    }

    private void parseFromItem() {
        parseTableReference();
        if (currentTokenType() == SQLTokenTypes.LITERAL_cross) {
            expectAndConsume(SQLTokenTypes.LITERAL_cross);
            expectAndConsume(SQLTokenTypes.LITERAL_join);
            parseTableReference();
        } else if (currentTokenType() == SQLTokenTypes.LITERAL_inner) {
            expectAndConsume(SQLTokenTypes.LITERAL_inner);
            expectAndConsume(SQLTokenTypes.LITERAL_join);
            parseTableReference();
            expectAndConsume(SQLTokenTypes.LITERAL_on);
            parseCondition();
        } else if (currentTokenType() == SQLTokenTypes.LITERAL_left) {
            expectAndConsume(SQLTokenTypes.LITERAL_left);
            if (currentTokenType() == SQLTokenTypes.LITERAL_outer) {
                expectAndConsume(SQLTokenTypes.LITERAL_outer);
            }
            expectAndConsume(SQLTokenTypes.LITERAL_join);
            parseTableReference();
            expectAndConsume(SQLTokenTypes.LITERAL_on);
            parseCondition();
        }
    }

    public void parseTableReference() {
        expectAndConsume(SQLTokenTypes.IDENTIFIER);
        if (currentTokenType() == SQLTokenTypes.DOT) {
            expectAndConsume(SQLTokenTypes.DOT);
            expectAndConsume(SQLTokenTypes.IDENTIFIER);
        }

        if (currentTokenType() == SQLTokenTypes.IDENTIFIER) {
            expectAndConsume(SQLTokenTypes.IDENTIFIER);
        }
    }

    private int currentTokenType() {
        return ((Token) tokens.get(0)).getType();
    }
    
    public String remainingTokens() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator iter = tokens.iterator();
        while (iter.hasNext()) {
            Token token = (Token) iter.next();
            if (token.getType() == SQLTokenTypes.EOF) {
                break;
            }
            if (first) {
                first = false;
            } else {
                result.append(" ");
            }
            result.append(token.getText());
        }
        return result.toString();
    }

    public String debugTokens() {
        StringBuilder result = new StringBuilder();
        Iterator iter = tokens.iterator();
        while (iter.hasNext()) {
            Token token = (Token) iter.next();
            if (token.getType() == SQLTokenTypes.EOF) {
                break;
            }
            result.append(Tree.typeName(token.getType()));
            result.append(" ");
            result.append(token.getText());
            result.append("\n");
        }
        return result.toString();
    }

    private Token expectAndConsume(int expectedType) {
        Token token = (Token) tokens.get(0);
        if (token.getType() != expectedType) {
            throw new ParserException(
                "expected " +
                describeExpectation(expectedType) +
                " but got " +
                describeToken(token)
            );
        }
        tokens.remove(0);
        return token;
    }

    private String describeExpectation(int expectedType) {
        String niceTokenTypeName = niceTokenTypeName(expectedType);
        if (niceTokenTypeName != null) {
            return niceTokenTypeName;
        }

        return Tree.typeName(expectedType);
    }

    private String describeToken(Token token) {
        String niceTokenTypeName = niceTokenTypeName(token.getType());
        if (niceTokenTypeName != null) {
            return niceTokenTypeName;
        }

        if (token.getType() == SQLTokenTypes.NUMBER) {
            return token.getText();
        } else {
            return Tree.typeName(token.getType());
        }
    }

    private String niceTokenTypeName(int type) {
        if (type == SQLTokenTypes.LITERAL_on) {
            return "ON";
        }
        return null;
    }

}
