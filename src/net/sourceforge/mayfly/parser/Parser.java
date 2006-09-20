package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.BinaryCell;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.constraint.Action;
import net.sourceforge.mayfly.datastore.constraint.Cascade;
import net.sourceforge.mayfly.datastore.constraint.NoAction;
import net.sourceforge.mayfly.datastore.constraint.SetDefault;
import net.sourceforge.mayfly.datastore.constraint.SetNull;
import net.sourceforge.mayfly.datastore.types.DataType;
import net.sourceforge.mayfly.datastore.types.DateDataType;
import net.sourceforge.mayfly.datastore.types.DefaultDataType;
import net.sourceforge.mayfly.datastore.types.UnimplementedDataType;
import net.sourceforge.mayfly.evaluation.Aggregator;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.GroupBy;
import net.sourceforge.mayfly.evaluation.GroupItem;
import net.sourceforge.mayfly.evaluation.NoGroupBy;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.CreateSchema;
import net.sourceforge.mayfly.evaluation.command.CreateTable;
import net.sourceforge.mayfly.evaluation.command.Delete;
import net.sourceforge.mayfly.evaluation.command.DropTable;
import net.sourceforge.mayfly.evaluation.command.Insert;
import net.sourceforge.mayfly.evaluation.command.InsertTable;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.SetSchema;
import net.sourceforge.mayfly.evaluation.command.Update;
import net.sourceforge.mayfly.evaluation.expression.Average;
import net.sourceforge.mayfly.evaluation.expression.Concatenate;
import net.sourceforge.mayfly.evaluation.expression.Count;
import net.sourceforge.mayfly.evaluation.expression.Divide;
import net.sourceforge.mayfly.evaluation.expression.Maximum;
import net.sourceforge.mayfly.evaluation.expression.Minimum;
import net.sourceforge.mayfly.evaluation.expression.Minus;
import net.sourceforge.mayfly.evaluation.expression.Multiply;
import net.sourceforge.mayfly.evaluation.expression.NullExpression;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.Sum;
import net.sourceforge.mayfly.evaluation.expression.UnimplementedExpression;
import net.sourceforge.mayfly.evaluation.expression.literal.CellExpression;
import net.sourceforge.mayfly.evaluation.expression.literal.DecimalLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.Literal;
import net.sourceforge.mayfly.evaluation.expression.literal.LongLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.evaluation.from.From;
import net.sourceforge.mayfly.evaluation.from.FromElement;
import net.sourceforge.mayfly.evaluation.from.FromTable;
import net.sourceforge.mayfly.evaluation.from.InnerJoin;
import net.sourceforge.mayfly.evaluation.from.LeftJoin;
import net.sourceforge.mayfly.evaluation.select.ColumnOrderItem;
import net.sourceforge.mayfly.evaluation.select.Limit;
import net.sourceforge.mayfly.evaluation.select.OrderBy;
import net.sourceforge.mayfly.evaluation.select.OrderItem;
import net.sourceforge.mayfly.evaluation.select.ReferenceOrderItem;
import net.sourceforge.mayfly.evaluation.select.Select;
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
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.StringBuilder;

import java.io.Reader;
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
 */
public class Parser {

    private List tokens;
    private final boolean allowParameters;

    public Parser(String sql) {
        this(new Lexer(sql).tokens());
    }
    
    /**
     * Create a parser which reads input from a Reader.
     * The caller is responsible for closing the Reader.
     */
    public Parser(Reader sql) {
        this(new Lexer(sql).tokens());
    }

    /**
     * Create a parser from a list of tokens.  The parser
     * will mutate the list as it parses.
     * The list must end with an end of file token.
     */
    public Parser(List tokens) {
        this(tokens, false);
    }
    
    public Parser(List tokens, boolean allowParameters) {
        this.tokens = tokens;
        this.allowParameters = allowParameters;
    }

    public List parseCommands() {
        List commands = new ArrayList();
        while (true) {
            if (consumeIfMatches(TokenType.END_OF_FILE)) {
                return commands;
            }
            else if (consumeIfMatches(TokenType.SEMICOLON)) {
                
            }
            else {
                commands.add(parseCommand());
            }
        }
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
                throw new ParserException("create command",
                    currentToken());
            }
        }
        else if (currentTokenType() == TokenType.KEYWORD_set) {
            return parseSetSchema();
        }
        else if (currentTokenType() == TokenType.KEYWORD_insert) {
            return parseInsert();
        }
        else if (currentTokenType() == TokenType.KEYWORD_update) {
            return parseUpdate();
        }
        else if (currentTokenType() == TokenType.KEYWORD_delete) {
            return parseDelete();
        }
        else {
            throw new ParserException("command", currentToken());
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

        ImmutableList columnNames = parseOptionalColumnNames();
        
        ImmutableList values = parseValueConstructor();

        return new Insert(table, columnNames, values);
    }

    private Command parseUpdate() {
        expectAndConsume(TokenType.KEYWORD_update);
        InsertTable table = parseInsertTable();
        expectAndConsume(TokenType.KEYWORD_set);
        List setClauses = parseSetClauseList();
        Where where = parseOptionalWhere();
        return new Update(table, setClauses, where);
    }
    
    private Command parseDelete() {
        expectAndConsume(TokenType.KEYWORD_delete);
        expectAndConsume(TokenType.KEYWORD_from);
        InsertTable table = parseInsertTable();
        Where where = parseOptionalWhere();
        return new Delete(table, where);
    }

    private List parseSetClauseList() {
        List clauses = new ArrayList();
        do {
            clauses.add(parseSetClause());
        } while (consumeIfMatches(TokenType.COMMA));
        return clauses;
    }

    private SetClause parseSetClause() {
        String column = consumeIdentifier();
        expectAndConsume(TokenType.EQUAL);
        Expression value = parseExpressionOrNull();
        return new SetClause(column, value);
    }

    private ImmutableList parseOptionalColumnNames() {
        if (currentTokenType() == TokenType.OPEN_PAREN) {
            return parseColumnNames();
        }
        else {
            return null;
        }
    }

    private ImmutableList parseColumnNames() {
        expectAndConsume(TokenType.OPEN_PAREN);

        List columnNames = new ArrayList();
        do {
            columnNames.add(consumeIdentifier());
        } while (consumeIfMatches(TokenType.COMMA));

        expectAndConsume(TokenType.CLOSE_PAREN);
        return new ImmutableList(columnNames);
    }

    private ImmutableList parseValueConstructor() {
        expectAndConsume(TokenType.KEYWORD_values);

        List values = new ArrayList();
        expectAndConsume(TokenType.OPEN_PAREN);

        do {
            values.add(parseAndEvaluate());
        } while (consumeIfMatches(TokenType.COMMA));
        expectAndConsume(TokenType.CLOSE_PAREN);
        return new ImmutableList(values);
    }

    private Cell parseAndEvaluate() {
        Expression expression = parseExpressionOrNull();
        if (expression == null) {
            // default value
            return null;
        }
        else {
            return expression.evaluate(null);
        }
    }

    Expression parseExpressionOrNull() {
        Location start = currentToken().location;

        if (consumeIfMatches(TokenType.KEYWORD_null)) {
            return new NullExpression(start);
        }
        else if (consumeIfMatches(TokenType.KEYWORD_default)) {
            return null;
        }
        else {
            try {
                return parseExpression().asNonBoolean();
            } catch (FoundNullLiteral e) {
                throw new MayflyException("Specify a null literal rather than an expression containing one");
            }
        }
    }

    private Command parseDrop() {
        expectAndConsume(TokenType.KEYWORD_drop);
        expectAndConsume(TokenType.KEYWORD_table);
        
        boolean ifExists = false;
        if (consumeIfMatches(TokenType.KEYWORD_if)) {
            expectAndConsume(TokenType.KEYWORD_exists);
            ifExists = true;
        }
        String tableName = consumeIdentifier();
        if (consumeIfMatches(TokenType.KEYWORD_if)) {
            expectAndConsume(TokenType.KEYWORD_exists);
            ifExists = true;
        }
        return new DropTable(tableName, ifExists);
    }

    private CreateSchema parseCreateSchema() {
        String schemaName = consumeIdentifier();
        if (consumeIfMatches(TokenType.KEYWORD_authorization)) {
            String user = consumeIdentifier();
            if (!user.equalsIgnoreCase("dba")) {
                throw new MayflyException("Can only specify user dba in create schema but was " + user);
            }
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
        CreateTable table = new CreateTable(tableName);
        expectAndConsume(TokenType.OPEN_PAREN);

        do {
            parseTableElement(table);
        } while (consumeIfMatches(TokenType.COMMA));

        expectAndConsume(TokenType.CLOSE_PAREN);
        
        parseTableTypeIfPresent();
        parseCharacterSetIfPresent();

        return table;
    }

    private void parseTableTypeIfPresent() {
        if (consumeNonReservedWordIfMatches("engine")) {
            expectAndConsume(TokenType.EQUAL);
            String tableType = consumeIdentifier();
            if ("innodb".equalsIgnoreCase(tableType)
                || "myisam".equalsIgnoreCase(tableType)) {
                // For now, ignore the type
            }
            else {
                throw new ParserException("unrecognized table type " + tableType);
            }
        }
    }

    private void parseCharacterSetIfPresent() {
        if (consumeIfMatches(TokenType.KEYWORD_character)) {
            expectAndConsume(TokenType.KEYWORD_set);
            consumeIdentifier();
        }
    }

    void parseTableElement(CreateTable table) {
        if (currentTokenType() == TokenType.IDENTIFIER) {
            table.addColumn(parseColumnDefinition(table));
        }
        else if (currentTokenType() == TokenType.KEYWORD_primary
            || currentTokenType() == TokenType.KEYWORD_unique
            || currentTokenType() == TokenType.KEYWORD_foreign
            || currentTokenType() == TokenType.KEYWORD_constraint
            ) {
            parseConstraint(table);
        }
        else {
            throw new ParserException(
                "column or table constraint",
                currentToken());
        }
    }

    private void parseConstraint(CreateTable table) {
        if (consumeIfMatches(TokenType.KEYWORD_constraint)) {
            consumeIdentifier();
        }

        if (consumeIfMatches(TokenType.KEYWORD_primary)) {
            expectAndConsume(TokenType.KEYWORD_key);
            table.setPrimaryKey(parseColumnNames());
        }
        else if (consumeIfMatches(TokenType.KEYWORD_unique)) {
            table.addUniqueConstraint(parseColumnNames());
        }
        else if (currentTokenType() == TokenType.KEYWORD_foreign) {
            parseForeignKeyConstraint(table);
        }
        else {
            throw new MayflyInternalException(
                "expected constraint but got " + currentToken().describe());
        }
    }

    private void parseForeignKeyConstraint(CreateTable table) {
        expectAndConsume(TokenType.KEYWORD_foreign);
        expectAndConsume(TokenType.KEYWORD_key);
        expectAndConsume(TokenType.OPEN_PAREN);
        String referencingColumn = consumeIdentifier();
        expectAndConsume(TokenType.CLOSE_PAREN);

        expectAndConsume(TokenType.KEYWORD_references);
        InsertTable targetTable = parseInsertTable();
        expectAndConsume(TokenType.OPEN_PAREN);
        String targetColumn = consumeIdentifier();
        expectAndConsume(TokenType.CLOSE_PAREN);
        
        Actions actions = parseActions();
        table.addForeignKeyConstraint(referencingColumn, 
            targetTable, targetColumn,
            actions.onDelete, actions.onUpdate);
    }

    Actions parseActions() {
        Actions actions = new Actions();
        if (consumeIfMatches(TokenType.KEYWORD_on)) {
            if (consumeIfMatches(TokenType.KEYWORD_delete)) {
                actions.onDelete = parseForeignKeyAction();
                if (consumeIfMatches(TokenType.KEYWORD_on)) {
                    expectAndConsume(TokenType.KEYWORD_update);
                    actions.onUpdate = parseForeignKeyAction();
                }
            }
            else if (consumeIfMatches(TokenType.KEYWORD_update)) {
                actions.onUpdate = parseForeignKeyAction();
                if (consumeIfMatches(TokenType.KEYWORD_on)) {
                    expectAndConsume(TokenType.KEYWORD_delete);
                    actions.onDelete = parseForeignKeyAction();
                }
            }
            else {
                throw new ParserException(
                    "UPDATE or DELETE",
                    currentToken());
            }
        }
        return actions;
    }
    
    class Actions {
        Action onDelete;
        Action onUpdate;

        public Actions() {
            onDelete = new NoAction();
            onUpdate = new NoAction();
        }
        
    }

    private Action parseForeignKeyAction() {
        if (consumeNonReservedWordIfMatches("no")) {
            expectNonReservedWord("action");
            return new NoAction();
        }
        else if (consumeNonReservedWordIfMatches("cascade")) {
            return new Cascade();
        }
        else if (consumeIfMatches(TokenType.KEYWORD_set)) {
            if (consumeIfMatches(TokenType.KEYWORD_null)) {
                return new SetNull();
            }
            else if (consumeIfMatches(TokenType.KEYWORD_default)) {
                return new SetDefault();
            }
            else {
                throw new ParserException("expected ON DELETE action " +
                    " but got SET " + currentToken().describe());
            }
        }
        else {
            throw new ParserException("ON DELETE action",
                currentToken());
        }
    }

    Column parseColumnDefinition(CreateTable table) {
        String name = consumeIdentifier();
        ParsedDataType parsed = parseDataType();
        boolean isAutoIncrement = parsed.isAutoIncrement;

        Cell defaultValue = parseDefaultClause(name);
        
        Cell onUpdateValue = parseOnUpdateValue(name);

        if (consumeNonReservedWordIfMatches("auto_increment")) {
            isAutoIncrement = true;
        }

        if (isAutoIncrement) {
            defaultValue = new LongCell(1);
        }

        parseColumnConstraints(table, name);

        return new Column(table.table(), name, defaultValue, onUpdateValue,
            isAutoIncrement, parsed.type);
    }

    private Cell parseDefaultClause(String name) {
        if (consumeIfMatches(TokenType.KEYWORD_default)) {
            Expression expression = parseDefaultValue(name);
            return expression.evaluate(null);
        }
        else {
            return NullCell.INSTANCE;
        }
    }
    
    private Cell parseOnUpdateValue(String columnName) {
        if (consumeIfMatches(TokenType.KEYWORD_on)) {
            expectAndConsume(TokenType.KEYWORD_update);
            return parseDefaultValue(columnName).evaluate(null);
        }
        else {
            return null;
        }
    }

    Expression parseDefaultValue(String columnName) {
        Location start = currentToken().location;

        if (currentTokenType() == TokenType.NUMBER || 
            currentTokenType() == TokenType.PERIOD) {
            return parseNumber(start).asNonBoolean();
        }
        else if (consumeIfMatches(TokenType.PLUS)) {
            return parseNumber(start).asNonBoolean();
        }
        else if (consumeIfMatches(TokenType.MINUS)) {
            return parseNegativeNumber(start).asNonBoolean();
        }
        else if (currentTokenType() == TokenType.QUOTED_STRING) {
            return parseQuotedString().asNonBoolean();
        }
        else if (consumeIfMatches(TokenType.KEYWORD_null)) {
            return new NullExpression(start);
        }
        else if (currentTokenType() == TokenType.KEYWORD_current_timestamp) {
            Token token = expectAndConsume(TokenType.KEYWORD_current_timestamp);
            return new UnimplementedExpression(token.getText());
        }
        else {
            throw new ParserException(
                "default value for column " + columnName,
                currentToken());
        }
    }

    private void parseColumnConstraints(CreateTable table, String column) {
        while (true) {
            if (consumeIfMatches(TokenType.KEYWORD_primary)) {
                expectAndConsume(TokenType.KEYWORD_key);
                table.setPrimaryKey(Collections.singletonList(column));
            }
            else if (consumeIfMatches(TokenType.KEYWORD_unique)) {
                table.addUniqueConstraint(Collections.singletonList(column));
            }
            else if (consumeIfMatches(TokenType.KEYWORD_not)) {
                expectAndConsume(TokenType.KEYWORD_null);
                table.addNotNullConstraint(column);
            }
            else {
                break;
            }
        }
    }

    ParsedDataType parseDataType() {
        boolean isAutoIncrement = false;
        DataType type = new DefaultDataType();
        if (consumeIfMatches(TokenType.KEYWORD_integer)) {
        }
        else if (consumeIfMatches(TokenType.KEYWORD_int)) {
        }
        else if (consumeIfMatches(TokenType.KEYWORD_smallint)) {
        }
        else if (consumeIfMatches(TokenType.KEYWORD_varchar)) {
            expectAndConsume(TokenType.OPEN_PAREN);
            expectAndConsume(TokenType.NUMBER);
            expectAndConsume(TokenType.CLOSE_PAREN);
        }
        else if (consumeIfMatches(TokenType.KEYWORD_decimal)) {
            expectAndConsume(TokenType.OPEN_PAREN);
            expectAndConsume(TokenType.NUMBER);
            expectAndConsume(TokenType.COMMA);
            expectAndConsume(TokenType.NUMBER);
            expectAndConsume(TokenType.CLOSE_PAREN);
        }
        else if (currentTokenType() == TokenType.IDENTIFIER) {
            // These shouldn't be reserved if they are not in the
            // SQL standard, seems like.
            Token token = expectAndConsume(TokenType.IDENTIFIER);
            String currentText = token.getText();
            if (currentText.equalsIgnoreCase("tinyint")) {
            }
            else if (currentText.equalsIgnoreCase("bigint")) {
            }
            else if (currentText.equalsIgnoreCase("text")) {
            }
            else if (currentText.equalsIgnoreCase("blob")) {
                if (consumeIfMatches(TokenType.OPEN_PAREN)) {
                    expectAndConsume(TokenType.NUMBER);
                    expectAndConsume(TokenType.CLOSE_PAREN);
                }
            }
            else if (currentText.equalsIgnoreCase("date")) {
                // This is a reserved word in SQL92, I think for the
                // DATE '2003-04-22' syntax for literals.
                // We could follow that lead if need be...
                type = new DateDataType();
            }
            else if (currentText.equalsIgnoreCase("timestamp")) {
                // Reserved word in SQL92; maybe we should too...
                type = new UnimplementedDataType(currentText);
            }
            else if (currentText.equalsIgnoreCase("identity")) {
                isAutoIncrement = true;
            }
            else if (currentText.equalsIgnoreCase("serial")) {
                isAutoIncrement = true;
            }
            else {
                throw new ParserException("data type", token);
            }
        }
        else {
            throw new ParserException("data type", currentToken());
        }
        return new ParsedDataType(isAutoIncrement, type);
    }
    
    class ParsedDataType {
        boolean isAutoIncrement;
        DataType type;

        public ParsedDataType(boolean isAutoIncrement, DataType type) {
            this.isAutoIncrement = isAutoIncrement;
            this.type = type;
        }
        
    }

    Select parseSelect() {
        expectAndConsume(TokenType.KEYWORD_select);
        What what = parseWhat();
        expectAndConsume(TokenType.KEYWORD_from);
        From from = parseFromItems();
        
        Where where = parseOptionalWhere();
        
        Aggregator groupBy = parseGroupBy();
        
        OrderBy orderBy = parseOrderBy();
        
        Limit limit = parseLimit();

        return new Select(what, from, where, groupBy, orderBy, limit);
    }

    private Where parseOptionalWhere() {
        if (consumeIfMatches(TokenType.KEYWORD_where)) {
            return parseWhere();
        }
        else {
            return Where.EMPTY;
        }
    }

    public What parseWhat() {
        if (consumeIfMatches(TokenType.ASTERISK)) {
            return new What(Collections.singletonList(new All()));
        }

        What what = new What();
        
        do {
            what.add(parseWhatElement());
        } while (consumeIfMatches(TokenType.COMMA));
        
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
        
        return parseExpression().asNonBoolean();
    }

    public ParserExpression parseExpression() {
        ParserExpression left = parseFactor();
        while (currentTokenType() == TokenType.MINUS
            || currentTokenType() == TokenType.PLUS
            ) {
            Token token = consume();
            if (token.getType() == TokenType.MINUS) {
                left = new NonBooleanParserExpression(new Minus(left.asNonBoolean(), parseFactor().asNonBoolean()));
            }
            else if (token.getType() == TokenType.PLUS) {
                left = new NonBooleanParserExpression(new Plus(left.asNonBoolean(), parseFactor().asNonBoolean()));
            }
            else {
                throw new MayflyInternalException("Didn't expect token " + token.describe());
            }
        }
        return left;
    }

    private ParserExpression parseFactor() {
        ParserExpression left = parsePrimary();
        while (currentTokenType() == TokenType.CONCATENATE
            || currentTokenType() == TokenType.DIVIDE
            || currentTokenType() == TokenType.ASTERISK
            ) {
            Token token = consume();
            if (token.getType() == TokenType.CONCATENATE) {
                left = new NonBooleanParserExpression(
                    new Concatenate(left.asNonBoolean(), parsePrimary().asNonBoolean())
                );
            }
            else if (token.getType() == TokenType.DIVIDE) {
                left = new NonBooleanParserExpression(
                    new Divide(left.asNonBoolean(), parsePrimary().asNonBoolean())
                );
            }
            else if (token.getType() == TokenType.ASTERISK) {
                left = new NonBooleanParserExpression(
                    new Multiply(left.asNonBoolean(), parsePrimary().asNonBoolean())
                );
            }
            else {
                throw new MayflyInternalException("Didn't expect token " + token.describe());
            }
        }
        return left;
    }

    public Where parseWhere() {
        return new Where(parseCondition().asBoolean());
    }

    public ParserExpression parseCondition() {
        ParserExpression expression = parseBooleanTerm();
        
        while (currentTokenType() == TokenType.KEYWORD_or) {
            expectAndConsume(TokenType.KEYWORD_or);
            BooleanExpression right = parseBooleanTerm().asBoolean();
            expression = new BooleanParserExpression(new Or(expression.asBoolean(), right));
        }

        return expression;
    }

    private ParserExpression parseBooleanTerm() {
        ParserExpression expression = parseBooleanFactor();
        
        while (currentTokenType() == TokenType.KEYWORD_and) {
            expectAndConsume(TokenType.KEYWORD_and);
            BooleanExpression right = parseBooleanFactor().asBoolean();
            expression = new BooleanParserExpression(new And(expression.asBoolean(), right));
        }

        return expression;
    }

    private ParserExpression parseBooleanFactor() {
        return parseBooleanPrimary();
    }

    private ParserExpression parseBooleanPrimary() {
        if (consumeIfMatches(TokenType.KEYWORD_not)) {
            BooleanExpression expression = parseBooleanPrimary().asBoolean();
            return new BooleanParserExpression(new Not(expression));
        }

        ParserExpression left = parseExpression();
        if (consumeIfMatches(TokenType.EQUAL)) {
            Expression right = parsePrimary().asNonBoolean();
            return new BooleanParserExpression(new Equal(left.asNonBoolean(), right));
        }
        else if (consumeIfMatches(TokenType.LESS_GREATER)) {
            Expression right = parsePrimary().asNonBoolean();
            return new BooleanParserExpression(NotEqual.construct(left.asNonBoolean(), right));
        }
        else if (consumeIfMatches(TokenType.BANG_EQUAL)) {
            Expression right = parsePrimary().asNonBoolean();
            return new BooleanParserExpression(NotEqual.construct(left.asNonBoolean(), right));
        }
        else if (consumeIfMatches(TokenType.GREATER)) {
            Expression right = parsePrimary().asNonBoolean();
            return new BooleanParserExpression(new Greater(left.asNonBoolean(), right));
        }
        else if (consumeIfMatches(TokenType.LESS)) {
            Expression right = parsePrimary().asNonBoolean();
            return new BooleanParserExpression(new Greater(right, left.asNonBoolean()));
        }
        else if (consumeIfMatches(TokenType.KEYWORD_not)) {
            return new BooleanParserExpression(new Not(parseIn(left.asNonBoolean())));
        }
        else if (currentTokenType() == TokenType.KEYWORD_in) {
            return new BooleanParserExpression(parseIn(left.asNonBoolean()));
        }
        else if (consumeIfMatches(TokenType.KEYWORD_is)) {
            if (consumeIfMatches(TokenType.KEYWORD_not)) {
                return new BooleanParserExpression(new Not(parseIs(left.asNonBoolean())));
            }
            return new BooleanParserExpression(parseIs(left.asNonBoolean()));
        }
        else {
            return left;
        }
    }

    private BooleanExpression parseIs(Expression left) {
        expectAndConsume(TokenType.KEYWORD_null);
        return new IsNull(left);
    }

    private BooleanExpression parseIn(Expression left) {
        expectAndConsume(TokenType.KEYWORD_in);
        expectAndConsume(TokenType.OPEN_PAREN);
        List expressions = parseExpressionList();
        expectAndConsume(TokenType.CLOSE_PAREN);
        return new In(left, expressions);
    }

    private List parseExpressionList() {
        List expressions = new ArrayList();
        do {
            expressions.add(parsePrimary().asNonBoolean());
        } while (consumeIfMatches(TokenType.COMMA));
        return expressions;
    }

    public ParserExpression parsePrimary() {
        AggregateArgumentParser argumentParser = new AggregateArgumentParser();
        Location start = currentToken().location;

        if (currentTokenType() == TokenType.IDENTIFIER) {
            return new NonBooleanParserExpression(parseColumnReference());
        }
        else if (currentTokenType() == TokenType.NUMBER || 
            currentTokenType() == TokenType.PERIOD) {
            return parseNumber(start);
        }
        else if (consumeIfMatches(TokenType.PLUS)) {
            return parseNumber(start);
        }
        else if (consumeIfMatches(TokenType.MINUS)) {
            return parseNegativeNumber(start);
        }
        else if (currentTokenType() == TokenType.QUOTED_STRING) {
            return parseQuotedString();
        }
        else if (consumeIfMatches(TokenType.PARAMETER)) {
            if (allowParameters) {
                // We are just doing a syntax check.
                return new NonBooleanParserExpression(new IntegerLiteral(0));
            }
            else {
                throw new MayflyException("Attempt to specify '?' outside a prepared statement");
            }
        }
        else if (consumeIfMatches(TokenType.KEYWORD_null)) {
            throw new FoundNullLiteral();
        }
        else if (currentTokenType() == TokenType.BINARY) {
            Token token = expectAndConsume(TokenType.BINARY);
            return new NonBooleanParserExpression(
                new CellExpression(new BinaryCell(token.getBytes()), start));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_max, false)) {
            return new NonBooleanParserExpression(new Maximum(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                argumentParser.location));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_min, false)) {
            return new NonBooleanParserExpression(new Minimum(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                argumentParser.location));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_sum, false)) {
            return new NonBooleanParserExpression(new Sum(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                argumentParser.location
            ));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_avg, false)) {
            return new NonBooleanParserExpression(new Average(
                (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                argumentParser.location
            ));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_count, true)) {
            if (argumentParser.gotAsterisk) {
                return new NonBooleanParserExpression(
                    new CountAll(argumentParser.functionName, argumentParser.location)
                );
            } else {
                return new NonBooleanParserExpression(new Count(
                    (SingleColumn) argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                    argumentParser.location
                ));
            }
        }
        else if (consumeIfMatches(TokenType.OPEN_PAREN)) {
            ParserExpression expression = parseCondition();
            expectAndConsume(TokenType.CLOSE_PAREN);
            return expression;
        }
        else {
            throw new ParserException("primary", currentToken());
        }
    }

    private ParserExpression parseQuotedString() {
        Token literal = expectAndConsume(TokenType.QUOTED_STRING);
        return new NonBooleanParserExpression(
            new QuotedString(literal.getText(), literal.location));
    }

    private ParserExpression parseNumber(Location start) {
        Literal number = parseNumericLiteral(start);
        return new NonBooleanParserExpression(number);
    }

    private ParserExpression parseNegativeNumber(Location start) {
        Literal number = parseNumericLiteral(start);
        return new NonBooleanParserExpression(makeNegativeLiteral(number));
    }

    private Literal makeNegativeLiteral(Literal number) {
        Location location = number.location;

        // Probably should have more unit tests for this, especially
        // the edge cases (-2^31 is one).
        if (number instanceof IntegerLiteral) {
            IntegerLiteral integer = (IntegerLiteral) number;
            return new IntegerLiteral(- integer.value, location);
        }
        else if (number instanceof LongLiteral) {
            LongLiteral longLiteral = (LongLiteral) number;
            return new LongLiteral(- longLiteral.value, location);
        }
        else {
            throw new UnimplementedException("don't yet handle big integers");
        }
    }

    Literal parseNumericLiteral(Location start) {
        if (consumeIfMatches(TokenType.PERIOD)) {
            Token number = expectAndConsume(TokenType.NUMBER);
            String secondInteger = number.getText();
            return new DecimalLiteral("." + secondInteger, 
                start.combine(number.location)
            );
        }

        Token number = expectAndConsume(TokenType.NUMBER);
        String firstInteger = number.getText();
        if (currentTokenType() == TokenType.PERIOD) {
            Token period = expectAndConsume(TokenType.PERIOD);
            if (currentTokenType() == TokenType.NUMBER) {
                Token secondNumber = expectAndConsume(TokenType.NUMBER);
                String secondInteger = secondNumber.getText();
                return new DecimalLiteral(
                    firstInteger + "." + secondInteger,
                    start.combine(secondNumber.location));
            }
            else {
                // Might need to look into semantics for this.  Should it be
                // BigDecimal or integer?
                return new DecimalLiteral(
                    firstInteger + ".",
                    start.combine(period.location)
                );
            }
        }
        return integerToObject(firstInteger, start.combine(number.location));
    }

    class AggregateArgumentParser {
        Expression expression;
        String functionName;
        boolean gotAsterisk;
        boolean distinct;
        Location location;

        boolean parse(TokenType aggregateTokenType, boolean allowAsterisk) {
            if (currentTokenType() == aggregateTokenType) {
                Token function = expectAndConsume(aggregateTokenType);
                functionName = function.getText();
                expectAndConsume(TokenType.OPEN_PAREN);
                if (allowAsterisk && consumeIfMatches(TokenType.ASTERISK)) {
                    gotAsterisk = true;
                } else {
                    if (consumeIfMatches(TokenType.KEYWORD_all)) {
                    }
                    else if (consumeIfMatches(TokenType.KEYWORD_distinct)) {
                        distinct = true;
                    }
                    expression = parseExpression().asNonBoolean();
                }
                Token end = expectAndConsume(TokenType.CLOSE_PAREN);

                location = function.location.combine(end.location);
                return true;
            }
            return false;
        }
    }

    abstract public class ParserExpression {

        abstract public Expression asNonBoolean();

        abstract public BooleanExpression asBoolean();

    }

    public class NonBooleanParserExpression extends ParserExpression {

        private final Expression expression;

        public NonBooleanParserExpression(Expression expression) {
            this.expression = expression;
        }

        public BooleanExpression asBoolean() {
            throw new ParserException("expected boolean expression but got non-boolean expression");
        }

        public Expression asNonBoolean() {
            return expression;
        }

    }

    public class BooleanParserExpression extends ParserExpression {

        private final BooleanExpression expression;

        public BooleanParserExpression(BooleanExpression expression) {
            this.expression = expression;
        }

        public BooleanExpression asBoolean() {
            return expression;
        }

        public Expression asNonBoolean() {
            throw new ParserException("expected non-boolean expression but got boolean expression");
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
        do {
            from.add(parseFromItem());
        } while (consumeIfMatches(TokenType.COMMA));
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
            do {
                groupBy.add(parseGroupItem());
            } while (consumeIfMatches(TokenType.COMMA));

            if (consumeIfMatches(TokenType.KEYWORD_having)) {
                groupBy.setHaving(parseCondition().asBoolean());
            }
            return groupBy;
        }
        else {
            if (consumeIfMatches(TokenType.KEYWORD_having)) {
                throw new ParserException("can't specify HAVING without GROUP BY");
            }
            return new NoGroupBy();
        }
    }

    private GroupItem parseGroupItem() {
        return new GroupItem(parseExpression().asNonBoolean());
    }

    private OrderBy parseOrderBy() {
        if (consumeIfMatches(TokenType.KEYWORD_order)) {
            expectAndConsume(TokenType.KEYWORD_by);
            
            OrderBy orderBy = new OrderBy();
            do {
                orderBy.add(parseOrderItem());
            } while (consumeIfMatches(TokenType.COMMA));
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
            
            if (consumeNonReservedWordIfMatches("offset")) {
                int offset = consumeInteger();
                return new Limit(count, offset);
            }
            
            return new Limit(count, Limit.NO_OFFSET);
        }
        else {
            return Limit.NONE;
        }
    }

    TokenType currentTokenType() {
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
            result.append(token.describe());
        }
        return result.toString();
    }

    private String consumeIdentifier() {
        Token token = expectAndConsume(TokenType.IDENTIFIER);
        return token.getText();
    }

    int consumeInteger() {
        Token number = expectAndConsume(TokenType.NUMBER);
        String text = number.getText();
        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
            // Out of range.  Most (all?) other cases are prevented in the lexer.
            throw new ParserException(text + " is out of range");
        }
    }

    private Literal integerToObject(String text, Location location) {
        try {
            return new IntegerLiteral(Integer.parseInt(text), location);
        }
        catch (NumberFormatException e) {
            // Out of range.  Most (all?) other cases are prevented in the lexer.
        }

        try {
            return new LongLiteral(Long.parseLong(text), location);
        }
        catch (NumberFormatException e) {
        }

        throw new UnimplementedException("don't yet handle BigInteger " + text);
    }

    boolean consumeIfMatches(TokenType type) {
        if (currentTokenType() == type) {
            expectAndConsume(type);
            return true;
        }
        return false;
    }
    
    private boolean consumeNonReservedWordIfMatches(String word) {
        if (currentTokenType() != TokenType.IDENTIFIER) {
            return false;
        }

        String currentText = currentToken().getText();
        if (word.equalsIgnoreCase(currentText)) {
            expectAndConsume(TokenType.IDENTIFIER);
            return true;
        }
        
        return false;
    }

    private void expectNonReservedWord(String word) {
        if (!consumeNonReservedWordIfMatches(word)) {
            throw new ParserException(word,
                currentToken());
        }
    }

    Token expectAndConsume(TokenType expectedType) {
        Token token = currentToken();
        if (token.getType() != expectedType) {
            throw new ParserException(
                describeExpectation(expectedType),
                token
            );
        }
        return consume();
    }

    /**
     * @internal
     * Consume the current token.  This is a slightly dangerous operation,
     * in the sense that it is easy to be careless about whether you are
     * consuming the token type that you think.  
     * So call {@link #expectAndConsume(TokenType)}
     * instead where feasible.
     */
    private Token consume() {
        return (Token) tokens.remove(0);
    }

    private String describeExpectation(TokenType expectedType) {
        return expectedType.description();
    }

}
