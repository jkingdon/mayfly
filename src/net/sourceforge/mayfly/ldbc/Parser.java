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

    public boolean isSelect() {
        return currentTokenType() == SQLTokenTypes.LITERAL_select;
    }

    public Select parseSelect() {
        expectAndConsume(SQLTokenTypes.LITERAL_select);
        What what = parseWhat();
        expectAndConsume(SQLTokenTypes.LITERAL_from);
        From from = parseFromItems();
        
        Where where;
        if (consumeIfMatches(SQLTokenTypes.LITERAL_where)) {
            where = parseWhere();
        }
        else {
            where = Where.EMPTY;
        }
        
        OrderBy orderBy = parseOrderBy();
        
        Limit limit = parseLimit();

        expectAndConsume(SQLTokenTypes.EOF);
        return new Select(what, from, where, orderBy, limit);
    }

    What parseWhat() {
        if (consumeIfMatches(SQLTokenTypes.ASTERISK)) {
            return new What(Collections.singletonList(new All()));
        }

        What what = new What();
        what.add(parseWhatElement());
        
        while (currentTokenType() == SQLTokenTypes.COMMA) {
            expectAndConsume(SQLTokenTypes.COMMA);
            what.add(parseWhatElement());
        }
        
        return what;
    }

    WhatElement parseWhatElement() {
        if (currentTokenType() == SQLTokenTypes.IDENTIFIER
            && ((Token) tokens.get(1)).getType() == SQLTokenTypes.DOT
            && ((Token) tokens.get(2)).getType() == SQLTokenTypes.ASTERISK) {

            String firstIdentifier = consumeIdentifier();
            expectAndConsume(SQLTokenTypes.DOT);
            expectAndConsume(SQLTokenTypes.ASTERISK);
            return new AllColumnsFromTable(firstIdentifier);
        }
        
        return parseExpression();
    }

    private WhatElement parseExpression() {
        WhatElement left = (WhatElement) parsePrimary();
        if (consumeIfMatches(SQLTokenTypes.VERTBARS)) {
            return new Concatenate(left, parseExpression());
        }
        return left;
    }

    public Where parseWhere() {
        return new Where(parseCondition());
    }

    public BooleanExpression parseCondition() {
        BooleanExpression firstTerm = parseBooleanTerm();
        
        if (currentTokenType() == SQLTokenTypes.LITERAL_or) {
            expectAndConsume(SQLTokenTypes.LITERAL_or);
            BooleanExpression right = parseCondition();
            return new Or(firstTerm, right);
        }

        return firstTerm;
    }

    private BooleanExpression parseBooleanTerm() {
        BooleanExpression firstFactor = parseBooleanFactor();
        
        if (currentTokenType() == SQLTokenTypes.LITERAL_and) {
            expectAndConsume(SQLTokenTypes.LITERAL_and);
            BooleanExpression right = parseBooleanTerm();
            return new And(firstFactor, right);
        }

        return firstFactor;
    }

    private BooleanExpression parseBooleanFactor() {
        return parseBooleanPrimary();
    }

    private BooleanExpression parseBooleanPrimary() {
        if (consumeIfMatches(SQLTokenTypes.LITERAL_not)) {
            BooleanExpression expression = parseBooleanPrimary();
            return new Not(expression);
        }

        if (consumeIfMatches(SQLTokenTypes.OPEN_PAREN)) {
            BooleanExpression expression = parseCondition();
            expectAndConsume(SQLTokenTypes.CLOSE_PAREN);
            return expression;
        }

        Transformer left = parsePrimary();
        if (consumeIfMatches(SQLTokenTypes.EQUAL)) {
            Transformer right = parsePrimary();
            return new Eq(left, right);
        }
        else if (consumeIfMatches(SQLTokenTypes.NOT_EQUAL)) {
            Transformer right = parsePrimary();
            return new Not(new Eq(left, right));
        }
        else if (consumeIfMatches(SQLTokenTypes.NOT_EQUAL_2)) {
            Transformer right = parsePrimary();
            return new Not(new Eq(left, right));
        }
        else if (consumeIfMatches(SQLTokenTypes.BIGGER)) {
            Transformer right = parsePrimary();
            return new Gt(left, right);
        }
        else if (consumeIfMatches(SQLTokenTypes.SMALLER)) {
            Transformer right = parsePrimary();
            return new Gt(right, left);
        }
        else if (consumeIfMatches(SQLTokenTypes.LITERAL_not)) {
            return new Not(parseIn(left));
        }
        else if (currentTokenType() == SQLTokenTypes.LITERAL_in) {
            return parseIn(left);
        }
        else if (consumeIfMatches(SQLTokenTypes.LITERAL_is)) {
            if (consumeIfMatches(SQLTokenTypes.LITERAL_not)) {
                return new Not(parseIs(left));
            }
            return parseIs(left);
        }
        else {
            throw new ParserException("expected boolean operator but got " + describeToken(currentToken()));
        }
    }

    private BooleanExpression parseIs(Transformer left) {
        expectAndConsume(SQLTokenTypes.LITERAL_null);
        return new IsNull(left);
    }

    private BooleanExpression parseIn(Transformer left) {
        expectAndConsume(SQLTokenTypes.LITERAL_in);
        expectAndConsume(SQLTokenTypes.OPEN_PAREN);
        List expressions = parseExpressionList();
        expectAndConsume(SQLTokenTypes.CLOSE_PAREN);
        return new In(left, expressions);
    }

    private List parseExpressionList() {
        List expressions = new ArrayList();
        expressions.add(parsePrimary());
        
        while (consumeIfMatches(SQLTokenTypes.COMMA)) {
            expressions.add(parsePrimary());
        }
        
        return expressions;
    }

    Transformer parsePrimary() {
        AggregateArgumentParser argumentParser = new AggregateArgumentParser();

        if (currentTokenType() == SQLTokenTypes.IDENTIFIER) {
            return parseColumnReference();
        }
        else if (currentTokenType() == SQLTokenTypes.NUMBER) {
            int number = consumeInteger();
            return new MathematicalInt(number);
        }
        else if (currentTokenType() == SQLTokenTypes.QUOTED_STRING) {
            Token literal = expectAndConsume(SQLTokenTypes.QUOTED_STRING);
            return new QuotedString(literal.getText());
        }
        else if (consumeIfMatches(SQLTokenTypes.PARAMETER)) {
            return JdbcParameter.INSTANCE;
        }
        else if (consumeIfMatches(SQLTokenTypes.LITERAL_null)) {
            throw new MayflyException("To check for null, use IS NULL or IS NOT NULL, not a null literal");
        }
        else if (argumentParser.parse(SQLTokenTypes.LITERAL_max, false)) {
            return new Max(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
        }
        else if (argumentParser.parse(SQLTokenTypes.LITERAL_min, false)) {
            return new Min(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
        }
        else if (argumentParser.parse(SQLTokenTypes.LITERAL_sum, false)) {
            return new Sum(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
        }
        else if (argumentParser.parse(SQLTokenTypes.LITERAL_avg, false)) {
            return new Average(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
        }
        else if (argumentParser.parse(SQLTokenTypes.LITERAL_count, true)) {
            if (argumentParser.gotAsterisk) {
                return new CountAll(argumentParser.functionName);
            } else {
                return new Count(
                    (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
            }
        }
        else {
            throw new ParserException("expected primary but got " + describeToken(currentToken()));
        }
    }

    class AggregateArgumentParser {
        WhatElement expression;
        String functionName;
        boolean gotAsterisk;
        boolean distinct;

        boolean parse(int aggregateTokenType, boolean allowAsterisk) {
            if (currentTokenType() == aggregateTokenType) {
                Token max = expectAndConsume(aggregateTokenType);
                functionName = max.getText();
                expectAndConsume(SQLTokenTypes.OPEN_PAREN);
                if (allowAsterisk && consumeIfMatches(SQLTokenTypes.ASTERISK)) {
                    gotAsterisk = true;
                } else {
                    if (consumeIfMatches(SQLTokenTypes.LITERAL_all)) {
                    }
                    else if (consumeIfMatches(SQLTokenTypes.LITERAL_distinct)) {
                        distinct = true;
                    }
                    expression = parseExpression();
                }
                expectAndConsume(SQLTokenTypes.CLOSE_PAREN);
                return true;
            }
            return false;
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
        if (consumeIfMatches(SQLTokenTypes.OPEN_PAREN)) {
            FromElement fromElement = parseFromItem();
            expectAndConsume(SQLTokenTypes.CLOSE_PAREN);
            return fromElement;
        }

        FromElement left = parseTableReference();
        while (true) {
            if (currentTokenType() == SQLTokenTypes.LITERAL_cross) {
                expectAndConsume(SQLTokenTypes.LITERAL_cross);
                expectAndConsume(SQLTokenTypes.LITERAL_join);
                FromElement right = parseFromItem();
                left = new InnerJoin(left, right, Where.EMPTY);
            }
            else if (currentTokenType() == SQLTokenTypes.LITERAL_inner) {
                expectAndConsume(SQLTokenTypes.LITERAL_inner);
                expectAndConsume(SQLTokenTypes.LITERAL_join);
                FromElement right = parseFromItem();
                expectAndConsume(SQLTokenTypes.LITERAL_on);
                Where condition = parseWhere();
                left = new InnerJoin(left, right, condition);
            }
            else if (currentTokenType() == SQLTokenTypes.LITERAL_left) {
                expectAndConsume(SQLTokenTypes.LITERAL_left);
                if (currentTokenType() == SQLTokenTypes.LITERAL_outer) {
                    expectAndConsume(SQLTokenTypes.LITERAL_outer);
                }
                expectAndConsume(SQLTokenTypes.LITERAL_join);
                FromElement right = parseFromItem();
                expectAndConsume(SQLTokenTypes.LITERAL_on);
                Where condition = parseWhere();
                left = new LeftJoin(left, right, condition);
            }
            else {
                return left;
            }
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

    private OrderBy parseOrderBy() {
        if (consumeIfMatches(SQLTokenTypes.LITERAL_order)) {
            expectAndConsume(SQLTokenTypes.LITERAL_by);
            
            OrderBy orderBy = new OrderBy();
            orderBy.add(parseOrderItem());
            
            while (consumeIfMatches(SQLTokenTypes.COMMA)) {
                orderBy.add(parseOrderItem());
            }
            return orderBy;
        }
        else {
            return new OrderBy();
        }
    }

    private OrderItem parseOrderItem() {
        SingleColumn column = parseColumnReference();
        if (consumeIfMatches(SQLTokenTypes.LITERAL_asc)) {
        } else if (consumeIfMatches(SQLTokenTypes.LITERAL_desc)) {
            return new OrderItem(column, false);
        }
        return new OrderItem(column, true);
    }

    private Limit parseLimit() {
        if (currentTokenType() == SQLTokenTypes.LITERAL_limit) {
            expectAndConsume(SQLTokenTypes.LITERAL_limit);
            int count = consumeInteger();
            
            if (consumeIfMatches(SQLTokenTypes.LITERAL_offset)) {
                int offset = consumeInteger();
                return new Limit(count, offset);
            }
            
            return new Limit(count, Limit.NO_OFFSET);
        }
        else {
            return Limit.NONE;
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

    private String consumeIdentifier() {
        Token token = expectAndConsume(SQLTokenTypes.IDENTIFIER);
        return token.getText();
    }

    private int consumeInteger() {
        Token number = expectAndConsume(SQLTokenTypes.NUMBER);
        return Integer.parseInt(number.getText());
    }

    private boolean consumeIfMatches(int type) {
        if (currentTokenType() == type) {
            expectAndConsume(type);
            return true;
        }
        return false;
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
        }
        else if (token.getType() == SQLTokenTypes.IDENTIFIER) {
            return token.getText();
        }
        else {
            return Tree.typeName(token.getType());
        }
    }

    private String niceTokenTypeName(int type) {
        if (type == SQLTokenTypes.LITERAL_on) {
            return "ON";
        }
        else if (type == SQLTokenTypes.LITERAL_from) {
            return "FROM";
        }
        else if (type == SQLTokenTypes.LITERAL_in) {
            return "IN";
        }
        else if (type == SQLTokenTypes.LITERAL_not) {
            return "NOT";
        }
        else if (type == SQLTokenTypes.LITERAL_null) {
            return "NULL";
        }
        return null;
    }

}
