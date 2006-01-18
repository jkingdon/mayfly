package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCellContent;
import net.sourceforge.mayfly.evaluation.Aggregator;
import net.sourceforge.mayfly.evaluation.ColumnOrderItem;
import net.sourceforge.mayfly.evaluation.GroupBy;
import net.sourceforge.mayfly.evaluation.GroupItem;
import net.sourceforge.mayfly.evaluation.NoGroupBy;
import net.sourceforge.mayfly.evaluation.ReferenceOrderItem;
import net.sourceforge.mayfly.evaluation.expression.Average;
import net.sourceforge.mayfly.evaluation.expression.Concatenate;
import net.sourceforge.mayfly.evaluation.expression.Count;
import net.sourceforge.mayfly.evaluation.expression.Divide;
import net.sourceforge.mayfly.evaluation.expression.Maximum;
import net.sourceforge.mayfly.evaluation.expression.Minimum;
import net.sourceforge.mayfly.evaluation.expression.Minus;
import net.sourceforge.mayfly.evaluation.expression.Multiply;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.Sum;
import net.sourceforge.mayfly.ldbc.Command;
import net.sourceforge.mayfly.ldbc.CreateSchema;
import net.sourceforge.mayfly.ldbc.CreateTable;
import net.sourceforge.mayfly.ldbc.DropTable;
import net.sourceforge.mayfly.ldbc.From;
import net.sourceforge.mayfly.ldbc.FromElement;
import net.sourceforge.mayfly.ldbc.FromTable;
import net.sourceforge.mayfly.ldbc.InnerJoin;
import net.sourceforge.mayfly.ldbc.Insert;
import net.sourceforge.mayfly.ldbc.InsertTable;
import net.sourceforge.mayfly.ldbc.JdbcParameter;
import net.sourceforge.mayfly.ldbc.LeftJoin;
import net.sourceforge.mayfly.ldbc.Limit;
import net.sourceforge.mayfly.ldbc.OrderBy;
import net.sourceforge.mayfly.ldbc.OrderItem;
import net.sourceforge.mayfly.ldbc.Select;
import net.sourceforge.mayfly.ldbc.SetSchema;
import net.sourceforge.mayfly.ldbc.what.All;
import net.sourceforge.mayfly.ldbc.what.AllColumnsFromTable;
import net.sourceforge.mayfly.ldbc.what.CountAll;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.what.What;
import net.sourceforge.mayfly.ldbc.what.WhatElement;
import net.sourceforge.mayfly.ldbc.where.And;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;
import net.sourceforge.mayfly.ldbc.where.Equal;
import net.sourceforge.mayfly.ldbc.where.Greater;
import net.sourceforge.mayfly.ldbc.where.In;
import net.sourceforge.mayfly.ldbc.where.IsNull;
import net.sourceforge.mayfly.ldbc.where.Not;
import net.sourceforge.mayfly.ldbc.where.NotEqual;
import net.sourceforge.mayfly.ldbc.where.Or;
import net.sourceforge.mayfly.ldbc.where.Where;
import net.sourceforge.mayfly.ldbc.where.literal.MathematicalInt;
import net.sourceforge.mayfly.ldbc.where.literal.QuotedString;
import net.sourceforge.mayfly.util.StringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

    private List tokens;

    public Parser(String sql) {
        this(new Lexer(sql).tokens());
    }

    public Parser(List tokens) {
        this.tokens = tokens;
    }

    public Command parse() {
        Command command = parseCommand();
        expectAndConsume(TokenType.END_OF_FILE);
        return command;
    }

    public Select parseQuery() {
        Select command = parseSelect();
        expectAndConsume(TokenType.END_OF_FILE);
        return command;
    }

    private Command parseCommand() {
        if (currentTokenType() == TokenType.KEYWORD_select) {
            return parseSelect();
        }
        else if (currentTokenType() == TokenType.KEYWORD_drop) {
            return parseDrop();
        }
        else if (consumeIfMatches(TokenType.KEYWORD_create)) {
            if (consumeIfMatches(TokenType.KEYWORD_schema)) {
                return parseCreateSchema();
            }
            else if (consumeIfMatches(TokenType.KEYWORD_table)) {
                return parseCreateTable();
            }
            else {
                throw new ParserException("expected create command but got " + describeToken(currentToken()));
            }
        }
        else if (currentTokenType() == TokenType.KEYWORD_set) {
            return parseSetSchema();
        }
        else if (currentTokenType() == TokenType.KEYWORD_insert) {
            return parseInsert();
        }
        else {
            throw new ParserException("expected command but got " + describeToken(currentToken()));
        }
    }

    private Command parseSetSchema() {
        expectAndConsume(TokenType.KEYWORD_set);
        expectAndConsume(TokenType.KEYWORD_schema);
        return new SetSchema(consumeIdentifier());
    }

    private Command parseInsert() {
        expectAndConsume(TokenType.KEYWORD_insert);
        expectAndConsume(TokenType.KEYWORD_into);
        InsertTable table = parseInsertTable();

        List columnNames = parseColumnNames();
        
        List values = parseValueConstructor();

        return new Insert(table, columnNames, values);
    }

    private List parseColumnNames() {
        List columnNames;
        if (consumeIfMatches(TokenType.OPEN_PAREN)) {
            columnNames = new ArrayList();
    
            columnNames.add(consumeIdentifier());
            while (consumeIfMatches(TokenType.COMMA)) {
                columnNames.add(consumeIdentifier());
            }
            expectAndConsume(TokenType.CLOSE_PAREN);
        }
        else {
            columnNames = null;
        }
        return columnNames;
    }

    private List parseValueConstructor() {
        expectAndConsume(TokenType.KEYWORD_values);

        List values = new ArrayList();
        expectAndConsume(TokenType.OPEN_PAREN);

        values.add(parseAndEvaluate());
        while (consumeIfMatches(TokenType.COMMA)) {
            values.add(parseAndEvaluate());
        }
        expectAndConsume(TokenType.CLOSE_PAREN);
        return values;
    }

    private Object parseAndEvaluate() {
        if (consumeIfMatches(TokenType.KEYWORD_null)) {
            return NullCellContent.INSTANCE;
        }

        try {
            WhatElement expression = parseExpression();
            if (expression instanceof JdbcParameter) {
                return JdbcParameter.INSTANCE;
            }
            Cell cell = expression.evaluate(null);
            return cell.asContents();
        } catch (FoundNullLiteral e) {
            throw new MayflyException(
                "To insert null, specify a null literal rather than an expression containing one"
            );
        }
    }

    private Command parseDrop() {
        expectAndConsume(TokenType.KEYWORD_drop);
        expectAndConsume(TokenType.KEYWORD_table);
        String tableName = consumeIdentifier();
        return new DropTable(tableName);
    }

    private CreateSchema parseCreateSchema() {
        String schemaName = consumeIdentifier();
        expectAndConsume(TokenType.KEYWORD_authorization);
        String user = consumeIdentifier();
        if (!user.equalsIgnoreCase("dba")) {
            throw new MayflyException("Can only create specify user dba in create schema but was " + user);
        }

        CreateSchema schema = new CreateSchema(schemaName);
        while (consumeIfMatches(TokenType.KEYWORD_create)) {
            expectAndConsume(TokenType.KEYWORD_table);
            CreateTable createTable = parseCreateTable();
            schema.add(createTable);
        }
        return schema;
    }

    private CreateTable parseCreateTable() {
        String tableName = consumeIdentifier();
        List columnNames = new ArrayList();
        expectAndConsume(TokenType.OPEN_PAREN);

        columnNames.add(parseColumnDefinition());
        while (consumeIfMatches(TokenType.COMMA)) {
            columnNames.add(parseColumnDefinition());
        }

        expectAndConsume(TokenType.CLOSE_PAREN);
        return new CreateTable(tableName, columnNames);
    }

    private String parseColumnDefinition() {
        String name = consumeIdentifier();
        if (consumeIfMatches(TokenType.KEYWORD_integer)) {
        }
        if (consumeIfMatches(TokenType.KEYWORD_varchar)) {
            expectAndConsume(TokenType.OPEN_PAREN);
            expectAndConsume(TokenType.NUMBER);
            expectAndConsume(TokenType.CLOSE_PAREN);
        }
        return name;
    }

    Select parseSelect() {
        expectAndConsume(TokenType.KEYWORD_select);
        What what = parseWhat();
        expectAndConsume(TokenType.KEYWORD_from);
        From from = parseFromItems();
        
        Where where;
        if (consumeIfMatches(TokenType.KEYWORD_where)) {
            where = parseWhere();
        }
        else {
            where = Where.EMPTY;
        }
        
        Aggregator groupBy = parseGroupBy();
        
        OrderBy orderBy = parseOrderBy();
        
        Limit limit = parseLimit();

        return new Select(what, from, where, groupBy, orderBy, limit);
    }

    public What parseWhat() {
        if (consumeIfMatches(TokenType.ASTERISK)) {
            return new What(Collections.singletonList(new All()));
        }

        What what = new What();
        what.add(parseWhatElement());
        
        while (currentTokenType() == TokenType.COMMA) {
            expectAndConsume(TokenType.COMMA);
            what.add(parseWhatElement());
        }
        
        return what;
    }

    public WhatElement parseWhatElement() {
        if (currentTokenType() == TokenType.IDENTIFIER
            && ((Token) tokens.get(1)).getType() == TokenType.PERIOD
            && ((Token) tokens.get(2)).getType() == TokenType.ASTERISK) {

            String firstIdentifier = consumeIdentifier();
            expectAndConsume(TokenType.PERIOD);
            expectAndConsume(TokenType.ASTERISK);
            return new AllColumnsFromTable(firstIdentifier);
        }
        
        return parseExpression();
    }

    WhatElement parseExpression() {
        WhatElement left = parseFactor();
        while (currentTokenType() == TokenType.MINUS
            || currentTokenType() == TokenType.PLUS
            ) {
            Token token = consume();
            if (token.getType() == TokenType.MINUS) {
                left = new Minus(left, parseFactor());
            }
            else if (token.getType() == TokenType.PLUS) {
                left = new Plus(left, parseFactor());
            }
            else {
                throw new MayflyInternalException("Didn't expect token " + describeToken(token));
            }
        }
        return left;
    }

    private WhatElement parseFactor() {
        WhatElement left = parsePrimary();
        while (currentTokenType() == TokenType.CONCATENATE
            || currentTokenType() == TokenType.DIVIDE
            || currentTokenType() == TokenType.ASTERISK
            ) {
            Token token = consume();
            if (token.getType() == TokenType.CONCATENATE) {
                left = new Concatenate(left, parsePrimary());
            }
            else if (token.getType() == TokenType.DIVIDE) {
                left = new Divide(left, parsePrimary());
            }
            else if (token.getType() == TokenType.ASTERISK) {
                left = new Multiply(left, parsePrimary());
            }
            else {
                throw new MayflyInternalException("Didn't expect token " + describeToken(token));
            }
        }
        return left;
    }

    public Where parseWhere() {
        return new Where(parseCondition());
    }

    public BooleanExpression parseCondition() {
        BooleanExpression expression = parseBooleanTerm();
        
        while (currentTokenType() == TokenType.KEYWORD_or) {
            expectAndConsume(TokenType.KEYWORD_or);
            BooleanExpression right = parseBooleanTerm();
            expression = new Or(expression, right);
        }

        return expression;
    }

    private BooleanExpression parseBooleanTerm() {
        BooleanExpression expression = parseBooleanFactor();
        
        while (currentTokenType() == TokenType.KEYWORD_and) {
            expectAndConsume(TokenType.KEYWORD_and);
            BooleanExpression right = parseBooleanFactor();
            expression = new And(expression, right);
        }

        return expression;
    }

    private BooleanExpression parseBooleanFactor() {
        return parseBooleanPrimary();
    }

    private BooleanExpression parseBooleanPrimary() {
        if (consumeIfMatches(TokenType.KEYWORD_not)) {
            BooleanExpression expression = parseBooleanPrimary();
            return new Not(expression);
        }

        if (consumeIfMatches(TokenType.OPEN_PAREN)) {
            BooleanExpression expression = parseCondition();
            expectAndConsume(TokenType.CLOSE_PAREN);
            return expression;
        }

        WhatElement left = parsePrimary();
        if (consumeIfMatches(TokenType.EQUAL)) {
            WhatElement right = parsePrimary();
            return new Equal(left, right);
        }
        else if (consumeIfMatches(TokenType.LESS_GREATER)) {
            WhatElement right = parsePrimary();
            return NotEqual.construct(left, right);
        }
        else if (consumeIfMatches(TokenType.BANG_EQUAL)) {
            WhatElement right = parsePrimary();
            return NotEqual.construct(left, right);
        }
        else if (consumeIfMatches(TokenType.GREATER)) {
            WhatElement right = parsePrimary();
            return new Greater(left, right);
        }
        else if (consumeIfMatches(TokenType.LESS)) {
            WhatElement right = parsePrimary();
            return new Greater(right, left);
        }
        else if (consumeIfMatches(TokenType.KEYWORD_not)) {
            return new Not(parseIn(left));
        }
        else if (currentTokenType() == TokenType.KEYWORD_in) {
            return parseIn(left);
        }
        else if (consumeIfMatches(TokenType.KEYWORD_is)) {
            if (consumeIfMatches(TokenType.KEYWORD_not)) {
                return new Not(parseIs(left));
            }
            return parseIs(left);
        }
        else {
            throw new ParserException("expected boolean operator but got " + describeToken(currentToken()));
        }
    }

    private BooleanExpression parseIs(WhatElement left) {
        expectAndConsume(TokenType.KEYWORD_null);
        return new IsNull(left);
    }

    private BooleanExpression parseIn(WhatElement left) {
        expectAndConsume(TokenType.KEYWORD_in);
        expectAndConsume(TokenType.OPEN_PAREN);
        List expressions = parseExpressionList();
        expectAndConsume(TokenType.CLOSE_PAREN);
        return new In(left, expressions);
    }

    private List parseExpressionList() {
        List expressions = new ArrayList();
        expressions.add(parsePrimary());
        
        while (consumeIfMatches(TokenType.COMMA)) {
            expressions.add(parsePrimary());
        }
        
        return expressions;
    }

    public WhatElement parsePrimary() {
        AggregateArgumentParser argumentParser = new AggregateArgumentParser();

        if (currentTokenType() == TokenType.IDENTIFIER) {
            return parseColumnReference();
        }
        else if (currentTokenType() == TokenType.NUMBER) {
            int number = consumeInteger();
            return new MathematicalInt(number);
        }
        else if (currentTokenType() == TokenType.QUOTED_STRING) {
            Token literal = expectAndConsume(TokenType.QUOTED_STRING);
            return new QuotedString(literal.getText());
        }
        else if (consumeIfMatches(TokenType.PARAMETER)) {
            return JdbcParameter.INSTANCE;
        }
        else if (consumeIfMatches(TokenType.KEYWORD_null)) {
            throw new FoundNullLiteral();
        }
        else if (argumentParser.parse(TokenType.KEYWORD_max, false)) {
            return new Maximum(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
        }
        else if (argumentParser.parse(TokenType.KEYWORD_min, false)) {
            return new Minimum(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
        }
        else if (argumentParser.parse(TokenType.KEYWORD_sum, false)) {
            return new Sum(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
        }
        else if (argumentParser.parse(TokenType.KEYWORD_avg, false)) {
            return new Average(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
        }
        else if (argumentParser.parse(TokenType.KEYWORD_count, true)) {
            if (argumentParser.gotAsterisk) {
                return new CountAll(argumentParser.functionName);
            } else {
                return new Count(
                    (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct);
            }
        }
        else if (consumeIfMatches(TokenType.OPEN_PAREN)) {
            WhatElement expression = parseExpression();
            expectAndConsume(TokenType.CLOSE_PAREN);
            return expression;
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

        boolean parse(TokenType aggregateTokenType, boolean allowAsterisk) {
            if (currentTokenType() == aggregateTokenType) {
                Token max = expectAndConsume(aggregateTokenType);
                functionName = max.getText();
                expectAndConsume(TokenType.OPEN_PAREN);
                if (allowAsterisk && consumeIfMatches(TokenType.ASTERISK)) {
                    gotAsterisk = true;
                } else {
                    if (consumeIfMatches(TokenType.KEYWORD_all)) {
                    }
                    else if (consumeIfMatches(TokenType.KEYWORD_distinct)) {
                        distinct = true;
                    }
                    expression = parseExpression();
                }
                expectAndConsume(TokenType.CLOSE_PAREN);
                return true;
            }
            return false;
        }
    }

    private SingleColumn parseColumnReference() {
        String firstIdentifier = consumeIdentifier();
        if (currentTokenType() == TokenType.PERIOD) {
            expectAndConsume(TokenType.PERIOD);
            String column = consumeIdentifier();
            return new SingleColumn(firstIdentifier, column);
        } else {
            return new SingleColumn(firstIdentifier);
        }
    }

    public From parseFromItems() {
        From from = new From();
        from.add(parseFromItem());
        
        while (currentTokenType() == TokenType.COMMA) {
            expectAndConsume(TokenType.COMMA);
            from.add(parseFromItem());
        }
        return from;
    }

    private FromElement parseFromItem() {
        if (consumeIfMatches(TokenType.OPEN_PAREN)) {
            FromElement fromElement = parseFromItem();
            expectAndConsume(TokenType.CLOSE_PAREN);
            return fromElement;
        }

        FromElement left = parseTableReference();
        while (true) {
            if (currentTokenType() == TokenType.KEYWORD_cross) {
                expectAndConsume(TokenType.KEYWORD_cross);
                expectAndConsume(TokenType.KEYWORD_join);
                FromElement right = parseFromItem();
                left = new InnerJoin(left, right, Where.EMPTY);
            }
            else if (currentTokenType() == TokenType.KEYWORD_inner) {
                expectAndConsume(TokenType.KEYWORD_inner);
                expectAndConsume(TokenType.KEYWORD_join);
                FromElement right = parseFromItem();
                expectAndConsume(TokenType.KEYWORD_on);
                Where condition = parseWhere();
                left = new InnerJoin(left, right, condition);
            }
            else if (currentTokenType() == TokenType.KEYWORD_left) {
                expectAndConsume(TokenType.KEYWORD_left);
                if (currentTokenType() == TokenType.KEYWORD_outer) {
                    expectAndConsume(TokenType.KEYWORD_outer);
                }
                expectAndConsume(TokenType.KEYWORD_join);
                FromElement right = parseFromItem();
                expectAndConsume(TokenType.KEYWORD_on);
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
        if (currentTokenType() == TokenType.PERIOD) {
            expectAndConsume(TokenType.PERIOD);
            table = consumeIdentifier();
        } else {
            table = firstIdentifier;
        }

        if (currentTokenType() == TokenType.IDENTIFIER) {
            String alias = consumeIdentifier();
            return new FromTable(table, alias);
        } else {
            return new FromTable(table);
        }
    }
    
    public InsertTable parseInsertTable() {
        String first = consumeIdentifier();
        if (consumeIfMatches(TokenType.PERIOD)) {
            String table = consumeIdentifier();
            return new InsertTable(first, table);
        }
        else {
            return new InsertTable(first);
        }
    }

    private Aggregator parseGroupBy() {
        if (consumeIfMatches(TokenType.KEYWORD_group)) {
            expectAndConsume(TokenType.KEYWORD_by);
            
            GroupBy groupBy = new GroupBy();
            groupBy.add(parseGroupItem());
            
            while (consumeIfMatches(TokenType.COMMA)) {
                groupBy.add(parseGroupItem());
            }
            return groupBy;
        }
        else {
            return new NoGroupBy();
        }
    }

    private GroupItem parseGroupItem() {
        return new GroupItem(parseColumnReference());
    }

    private OrderBy parseOrderBy() {
        if (consumeIfMatches(TokenType.KEYWORD_order)) {
            expectAndConsume(TokenType.KEYWORD_by);
            
            OrderBy orderBy = new OrderBy();
            orderBy.add(parseOrderItem());
            
            while (consumeIfMatches(TokenType.COMMA)) {
                orderBy.add(parseOrderItem());
            }
            return orderBy;
        }
        else {
            return new OrderBy();
        }
    }

    private OrderItem parseOrderItem() {
        if (currentTokenType() == TokenType.NUMBER) {
            int reference = consumeInteger();
            boolean ascending = parseAscending();
            return new ReferenceOrderItem(reference, ascending);
        }
        else {
            SingleColumn column = parseColumnReference();
            boolean ascending = parseAscending();
            return new ColumnOrderItem(column, ascending);
        }
    }

    private boolean parseAscending() {
        if (consumeIfMatches(TokenType.KEYWORD_asc)) {
            return true;
        }
        else if (consumeIfMatches(TokenType.KEYWORD_desc)) {
            return false;
        }
        else {
            return true;
        }
    }

    private Limit parseLimit() {
        if (currentTokenType() == TokenType.KEYWORD_limit) {
            expectAndConsume(TokenType.KEYWORD_limit);
            int count = consumeInteger();
            
            if (consumeIfMatches(TokenType.KEYWORD_offset)) {
                int offset = consumeInteger();
                return new Limit(count, offset);
            }
            
            return new Limit(count, Limit.NO_OFFSET);
        }
        else {
            return Limit.NONE;
        }
    }

    private TokenType currentTokenType() {
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
            if (token.getType() == TokenType.END_OF_FILE) {
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
            if (token.getType() == TokenType.END_OF_FILE) {
                break;
            }
            result.append(token.getType().description());
            result.append(" ");
            result.append(token.getText());
            result.append("\n");
        }
        return result.toString();
    }

    private String consumeIdentifier() {
        Token token = expectAndConsume(TokenType.IDENTIFIER);
        return token.getText();
    }

    private int consumeInteger() {
        Token number = expectAndConsume(TokenType.NUMBER);
        return Integer.parseInt(number.getText());
    }

    private boolean consumeIfMatches(TokenType type) {
        if (currentTokenType() == type) {
            expectAndConsume(type);
            return true;
        }
        return false;
    }

    private Token expectAndConsume(TokenType expectedType) {
        Token token = currentToken();
        if (token.getType() != expectedType) {
            throw new ParserException(
                "expected " +
                describeExpectation(expectedType) +
                " but got " +
                describeToken(token)
            );
        }
        return consume();
    }

    /**
     * @internal
     * Consume the current token.  This is a slightly dangerous operation,
     * in the sense that it is easy to be careless about whether you are
     * consuming the token type that you think.  So call {@link #expectAndConsume(int)}
     * instead where feasible.
     */
    private Token consume() {
        return (Token) tokens.remove(0);
    }

    private String describeExpectation(TokenType expectedType) {
        return expectedType.description();
    }

    private String describeToken(Token token) {
        TokenType type = token.getType();
        if (type == TokenType.NUMBER) {
            return token.getText();
        }
        else if (type == TokenType.IDENTIFIER) {
            return token.getText();
        }
        else {
            return type.description();
        }
    }

}
