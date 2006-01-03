package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;
import antlr.*;

import java.io.*;
import java.util.*;

/** @internal
 * Hand-written recursive descent parser.
 * So far this has brought far fewer headaches than ANTLR (which might
 * mean I just don't understand ANTLR).  It is also nicer to unit test
 * this parser, there is no crazy build.xml junk like with ANTLR, and
 * who knows what other benefits.
 * 
 * The lexer is still the ANTLR one, at least for now.
 */
public class Parser {

    private static List getAllTokens(String sql) {
        try {
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
        } catch (TokenStreamException e) {
            // Do we want to report the text we were parsing, or line/column numbers?
            // Is there anything interesting about e other than its message (like its class?)
            throw new MayflyException(e.getMessage());
        }
    }

    private List tokens;

    public Parser(String sql) {
        this(getAllTokens(sql));
    }

    public Parser(List tokens) {
        this.tokens = tokens;
    }

    public Select parseSelect() {
        expectAndConsume(SQLTokenTypes.LITERAL_select);
        What what = parseWhat();
        expectAndConsume(SQLTokenTypes.LITERAL_from);
        From from = parseFromItems();
        
        Where where;
        if (currentTokenType() == SQLTokenTypes.LITERAL_where) {
            expectAndConsume(SQLTokenTypes.LITERAL_where);
            where = parseWhere();
        } else {
            where = Where.EMPTY;
        }
        
        expectAndConsume(SQLTokenTypes.EOF);
        return new Select(what, from, where);
    }

    What parseWhat() {
        What what = new What();
        what.add(parseWhatElement());
        
        while (currentTokenType() == SQLTokenTypes.COMMA) {
            expectAndConsume(SQLTokenTypes.COMMA);
            what.add(parseWhatElement());
        }
        
        return what;
    }

    private WhatElement parseWhatElement() {
        if (currentTokenType() == SQLTokenTypes.ASTERISK) {
            expectAndConsume(SQLTokenTypes.ASTERISK);
            return new All();
        } else if (currentTokenType() == SQLTokenTypes.IDENTIFIER) {
            return parseColumnReference();
        } else {
            throw new ParserException("expected something to select, got " + describeToken(currentToken()));
        }
    }

    Where parseWhere() {
        return new Where(parseCondition());
    }

    private BooleanExpression parseCondition() {
        BooleanExpression firstTerm = parseBooleanTerm();
        
        if (currentTokenType() == SQLTokenTypes.LITERAL_or) {
            expectAndConsume(SQLTokenTypes.LITERAL_or);
            BooleanExpression right = parseBooleanTerm();
            return new Or(firstTerm, right);
        }

        return firstTerm;
    }

    private BooleanExpression parseBooleanTerm() {
        BooleanExpression firstFactor = parseBooleanFactor();
        
        if (currentTokenType() == SQLTokenTypes.LITERAL_and) {
            expectAndConsume(SQLTokenTypes.LITERAL_and);
            BooleanExpression right = parseBooleanFactor();
            return new And(firstFactor, right);
        }

        return firstFactor;
    }

    private BooleanExpression parseBooleanFactor() {
        return parseBooleanPrimary();
    }

    private BooleanExpression parseBooleanPrimary() {
        Transformer left = parsePrimary();
        expectAndConsume(SQLTokenTypes.EQUAL);
        Transformer right = parsePrimary();
        return new Eq(left, right);
    }

    Transformer parsePrimary() {
        if (currentTokenType() == SQLTokenTypes.IDENTIFIER) {
            return parseColumnReference();
        }
        else if (currentTokenType() == SQLTokenTypes.NUMBER) {
            Token number = expectAndConsume(SQLTokenTypes.NUMBER);
            return new MathematicalInt(Integer.parseInt(number.getText()));
        }
        else if (currentTokenType() == SQLTokenTypes.QUOTED_STRING) {
            Token literal = expectAndConsume(SQLTokenTypes.QUOTED_STRING);
            return new QuotedString(literal.getText());
        }
        else {
            throw new ParserException("expected primary, got " + describeToken(currentToken()));
        }
    }

    private SingleColumn parseColumnReference() {
        String firstIdentifier = consumeIdentifier();
        if (currentTokenType() == SQLTokenTypes.DOT) {
            expectAndConsume(SQLTokenTypes.DOT);
            String column = consumeIdentifier();
            return new SingleColumn(firstIdentifier, column);
        } else {
            return new SingleColumn(firstIdentifier);
        }
    }

    private String consumeIdentifier() {
        Token token = expectAndConsume(SQLTokenTypes.IDENTIFIER);
        return token.getText();
    }

    From parseFromItems() {
        From from = new From();
        from.add(parseFromItem());
        
        while (currentTokenType() == SQLTokenTypes.COMMA) {
            expectAndConsume(SQLTokenTypes.COMMA);
            from.add(parseFromItem());
        }
        return from;
    }

    private FromElement parseFromItem() {
        FromTable left = parseTableReference();
        if (currentTokenType() == SQLTokenTypes.LITERAL_cross) {
            expectAndConsume(SQLTokenTypes.LITERAL_cross);
            expectAndConsume(SQLTokenTypes.LITERAL_join);
            FromTable right = parseTableReference();
            return new InnerJoin(left, right, Where.EMPTY);
        }
        else if (currentTokenType() == SQLTokenTypes.LITERAL_inner) {
            expectAndConsume(SQLTokenTypes.LITERAL_inner);
            expectAndConsume(SQLTokenTypes.LITERAL_join);
            FromTable right = parseTableReference();
            expectAndConsume(SQLTokenTypes.LITERAL_on);
            Where condition = parseWhere();
            return new InnerJoin(left, right, condition);
        }
        else if (currentTokenType() == SQLTokenTypes.LITERAL_left) {
            expectAndConsume(SQLTokenTypes.LITERAL_left);
            if (currentTokenType() == SQLTokenTypes.LITERAL_outer) {
                expectAndConsume(SQLTokenTypes.LITERAL_outer);
            }
            expectAndConsume(SQLTokenTypes.LITERAL_join);
            FromTable right = parseTableReference();
            expectAndConsume(SQLTokenTypes.LITERAL_on);
            Where condition = parseWhere();
            return new LeftJoin(left, right, condition);
        }
        else {
            return left;
        }
    }

    public FromTable parseTableReference() {
        String firstIdentifier = consumeIdentifier();
        String table;
        if (currentTokenType() == SQLTokenTypes.DOT) {
            expectAndConsume(SQLTokenTypes.DOT);
            table = consumeIdentifier();
        } else {
            table = firstIdentifier;
        }

        if (currentTokenType() == SQLTokenTypes.IDENTIFIER) {
            String alias = consumeIdentifier();
            return new FromTable(table, alias);
        } else {
            return new FromTable(table);
        }
    }

    private int currentTokenType() {
        return currentToken().getType();
    }

    private Token currentToken() {
        return (Token) tokens.get(0);
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
        Token token = currentToken();
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
