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
import net.sourceforge.mayfly.datastore.types.BinaryDataType;
import net.sourceforge.mayfly.datastore.types.DataType;
import net.sourceforge.mayfly.datastore.types.DateDataType;
import net.sourceforge.mayfly.datastore.types.DefaultDataType;
import net.sourceforge.mayfly.datastore.types.TimestampDataType;
import net.sourceforge.mayfly.evaluation.Aggregator;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.GroupBy;
import net.sourceforge.mayfly.evaluation.GroupItem;
import net.sourceforge.mayfly.evaluation.NoGroupBy;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.command.AddColumn;
import net.sourceforge.mayfly.evaluation.command.AddConstraint;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.CreateSchema;
import net.sourceforge.mayfly.evaluation.command.CreateTable;
import net.sourceforge.mayfly.evaluation.command.Delete;
import net.sourceforge.mayfly.evaluation.command.DropColumn;
import net.sourceforge.mayfly.evaluation.command.DropForeignKey;
import net.sourceforge.mayfly.evaluation.command.DropTable;
import net.sourceforge.mayfly.evaluation.command.Insert;
import net.sourceforge.mayfly.evaluation.command.ModifyColumn;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.SetSchema;
import net.sourceforge.mayfly.evaluation.command.UnresolvedConstraint;
import net.sourceforge.mayfly.evaluation.command.UnresolvedForeignKey;
import net.sourceforge.mayfly.evaluation.command.UnresolvedPrimaryKey;
import net.sourceforge.mayfly.evaluation.command.UnresolvedTableReference;
import net.sourceforge.mayfly.evaluation.command.UnresolvedUniqueConstraint;
import net.sourceforge.mayfly.evaluation.command.Update;
import net.sourceforge.mayfly.evaluation.condition.And;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.condition.Equal;
import net.sourceforge.mayfly.evaluation.condition.Greater;
import net.sourceforge.mayfly.evaluation.condition.In;
import net.sourceforge.mayfly.evaluation.condition.IsNull;
import net.sourceforge.mayfly.evaluation.condition.Not;
import net.sourceforge.mayfly.evaluation.condition.NotEqual;
import net.sourceforge.mayfly.evaluation.condition.Or;
import net.sourceforge.mayfly.evaluation.expression.Average;
import net.sourceforge.mayfly.evaluation.expression.Concatenate;
import net.sourceforge.mayfly.evaluation.expression.Count;
import net.sourceforge.mayfly.evaluation.expression.CountAll;
import net.sourceforge.mayfly.evaluation.expression.Divide;
import net.sourceforge.mayfly.evaluation.expression.Maximum;
import net.sourceforge.mayfly.evaluation.expression.Minimum;
import net.sourceforge.mayfly.evaluation.expression.Minus;
import net.sourceforge.mayfly.evaluation.expression.Multiply;
import net.sourceforge.mayfly.evaluation.expression.NullExpression;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
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
import net.sourceforge.mayfly.evaluation.what.AliasedExpression;
import net.sourceforge.mayfly.evaluation.what.All;
import net.sourceforge.mayfly.evaluation.what.AllColumnsFromTable;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
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
        else if (consumeIfMatches(TokenType.KEYWORD_alter)) {
            expectAndConsume(TokenType.KEYWORD_table);
            return parseAlterTable();
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
        Location start = currentToken().location;

        expectAndConsume(TokenType.KEYWORD_insert);
        expectAndConsume(TokenType.KEYWORD_into);
        UnresolvedTableReference table = parseTableReference();

        ImmutableList columnNames = parseOptionalColumnNames();
        
        ValueList values = parseValueConstructor();

        return new Insert(table, columnNames, values, start.combine(values.location));
    }

    private Command parseUpdate() {
        expectAndConsume(TokenType.KEYWORD_update);
        UnresolvedTableReference table = parseTableReference();
        expectAndConsume(TokenType.KEYWORD_set);
        List setClauses = parseSetClauseList();
        Condition where = parseOptionalWhere();
        return new Update(table, setClauses, where);
    }
    
    private Command parseDelete() {
        expectAndConsume(TokenType.KEYWORD_delete);
        expectAndConsume(TokenType.KEYWORD_from);
        UnresolvedTableReference table = parseTableReference();
        Condition where = parseOptionalWhere();
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
    
    ValueList parseValueConstructor() {
        Location start = expectAndConsume(TokenType.KEYWORD_values).location;

        ValueList values = new ValueList(start);
        expectAndConsume(TokenType.OPEN_PAREN);

        do {
            values = values.with(parseAndEvaluate());
        } while (consumeIfMatches(TokenType.COMMA));
        Location end = expectAndConsume(TokenType.CLOSE_PAREN).location;
        return values.with(end);
    }

    private Value parseAndEvaluate() {
        Expression expression = parseExpressionOrNull();
        if (expression == null) {
            // default value
            return new Value(null, Location.UNKNOWN);
        }
        else {
            Cell cell = expression.evaluate(new ResultRow() {
                public SingleColumn findColumn(
                    String tableOrAlias, String columnName,
                    Location location) {
                    throw new MayflyException(
                        "values clause may not refer to column: " 
                        + Column.displayName(tableOrAlias, columnName)
                    );
                }
            }
            );
            return new Value(cell, expression.location);
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

    private Command parseAlterTable() {
        UnresolvedTableReference table = parseTableReference();
        if (consumeIfMatches(TokenType.KEYWORD_drop)) {
            // optional according to SQL92 but does anyone omit it?
            if (consumeIfMatches(TokenType.KEYWORD_column)) {
                String column = consumeIdentifier();
                return new DropColumn(table, column);
            }
            else if (consumeIfMatches(TokenType.KEYWORD_foreign)) {
                expectAndConsume(TokenType.KEYWORD_key);
                String constraintName = consumeIdentifier();
                return new DropForeignKey(table, constraintName);
            }
            else {
                throw new ParserException(
                    "alter table drop action", currentToken());
            }
        }
        else if (consumeIfMatches(TokenType.KEYWORD_add)) {
            // optional according to SQL92 but does anyone omit it?
            if (consumeIfMatches(TokenType.KEYWORD_column)) {
                Column newColumn = parseColumnDisallowingMostConstraints(table);
                return new AddColumn(table, newColumn);
            }
            else if (lookingAtConstraint()) {
                UnresolvedConstraint key = parseConstraint();
                return new AddConstraint(table, key);
            }
            else {
                throw new ParserException(
                    "alter table add action", currentToken());
            }
        }
        else if (consumeNonReservedWordIfMatches("modify")) {
            expectAndConsume(TokenType.KEYWORD_column);
            Column newColumn = parseColumnDisallowingMostConstraints(table);
            return new ModifyColumn(table, newColumn);
        }
        else {
            throw new ParserException("alter table action", currentToken());
        }
    }

    /**
     * @internal
     * Parse column and throw exception if a constraint is
     * present.
     * The exceptions are constraints which are stored in
     * the {@link Column} itself (currently NOT NULL).
     */
    private Column parseColumnDisallowingMostConstraints(
        UnresolvedTableReference table) {
        CreateTable constraintCollector = new CreateTable(table.tableName());
        Column newColumn = parseColumnDefinition(constraintCollector);
        if (constraintCollector.hasConstraints()) {
            throw new MayflyException(
                "constraints are not yet supported in ALTER TABLE");
        }
        return newColumn;
    }

    void parseTableElement(CreateTable table) {
        if (currentTokenType() == TokenType.IDENTIFIER) {
            table.addColumn(parseColumnDefinition(table));
        }
        else if (lookingAtConstraint()) {
            table.addConstraint(parseConstraint());
        }
        else {
            throw new ParserException(
                "column or table constraint",
                currentToken());
        }
    }

    private boolean lookingAtConstraint() {
        return currentTokenType() == TokenType.KEYWORD_primary
            || currentTokenType() == TokenType.KEYWORD_unique
            || currentTokenType() == TokenType.KEYWORD_foreign
            || currentTokenType() == TokenType.KEYWORD_constraint;
    }

    private UnresolvedConstraint parseConstraint() {
        String constraintName;
        if (consumeIfMatches(TokenType.KEYWORD_constraint)) {
            constraintName = consumeIdentifier();
        }
        else {
            constraintName = null;
        }

        if (consumeIfMatches(TokenType.KEYWORD_primary)) {
            expectAndConsume(TokenType.KEYWORD_key);
            return new UnresolvedPrimaryKey(
                parseColumnNames(), constraintName);
        }
        else if (consumeIfMatches(TokenType.KEYWORD_unique)) {
            return new UnresolvedUniqueConstraint(
                parseColumnNames(), constraintName);
        }
        else if (currentTokenType() == TokenType.KEYWORD_foreign) {
            return parseForeignKeyConstraint(constraintName);
        }
        else {
            throw new MayflyInternalException(
                "expected constraint but got " + currentToken().describe());
        }
    }

    private UnresolvedForeignKey parseForeignKeyConstraint(String constraintName) {
        Location start = currentToken().location;

        expectAndConsume(TokenType.KEYWORD_foreign);
        expectAndConsume(TokenType.KEYWORD_key);
        expectAndConsume(TokenType.OPEN_PAREN);
        String referencingColumn = consumeIdentifier();
        expectAndConsume(TokenType.CLOSE_PAREN);

        expectAndConsume(TokenType.KEYWORD_references);
        UnresolvedTableReference targetTable = parseTableReference();
        expectAndConsume(TokenType.OPEN_PAREN);
        String targetColumn = consumeIdentifier();
        Token end = expectAndConsume(TokenType.CLOSE_PAREN);
        
        Actions actions = parseActions();
        return new UnresolvedForeignKey(
            referencingColumn, targetTable, targetColumn, 
            actions.onDelete, actions.onUpdate,
            constraintName,
            start.combine(end.location)
        );
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

        boolean isNotNull = parseColumnConstraints(table, name);

        return new Column(name, defaultValue, onUpdateValue, isAutoIncrement,
            parsed.type, isNotNull);
    }

    private Cell parseDefaultClause(String name) {
        if (consumeIfMatches(TokenType.KEYWORD_default)) {
            Expression expression = parseDefaultValue(name);
            return expression.evaluate((ResultRow)null);
        }
        else {
            return NullCell.INSTANCE;
        }
    }
    
    private Cell parseOnUpdateValue(String columnName) {
        if (consumeIfMatches(TokenType.KEYWORD_on)) {
            expectAndConsume(TokenType.KEYWORD_update);
            return parseDefaultValue(columnName).evaluate((ResultRow)null);
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

    private boolean parseColumnConstraints(CreateTable table, String column) {
        boolean isNotNull = false;
        while (true) {
            if (consumeIfMatches(TokenType.KEYWORD_primary)) {
                expectAndConsume(TokenType.KEYWORD_key);
                table.addConstraint(new UnresolvedPrimaryKey(column));
            }
            else if (consumeIfMatches(TokenType.KEYWORD_unique)) {
                table.addConstraint(new UnresolvedUniqueConstraint(column));
            }
            else if (consumeIfMatches(TokenType.KEYWORD_not)) {
                expectAndConsume(TokenType.KEYWORD_null);
                isNotNull = true;
            }
            else {
                break;
            }
        }
        return isNotNull;
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
                    type = new BinaryDataType();
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
                type = new TimestampDataType();
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
        
        Condition where = parseOptionalWhere();
        
        Aggregator groupBy = parseGroupBy();
        
        OrderBy orderBy = parseOrderBy();
        
        Limit limit = parseLimit();
        
        if (consumeIfMatches(TokenType.KEYWORD_for)) {
            expectAndConsume(TokenType.KEYWORD_update);
            /* Until we try to do transactions, or at least
               multiple threads, this can be a noop */
        }

        return new Select(what, from, where, groupBy, orderBy, limit);
    }

    private Condition parseOptionalWhere() {
        if (consumeIfMatches(TokenType.KEYWORD_where)) {
            return parseWhere();
        }
        else {
            return Condition.TRUE;
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
        
        Expression expression = parseExpression().asNonBoolean();
        if (consumeIfMatches(TokenType.KEYWORD_as)) {
            String aliasedColumn = consumeIdentifier();
            return new AliasedExpression(aliasedColumn, expression);
        }
        return expression;
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

    public Condition parseWhere() {
        return parseCondition().asBoolean();
    }

    public ParserExpression parseCondition() {
        ParserExpression expression = parseBooleanTerm();
        
        while (currentTokenType() == TokenType.KEYWORD_or) {
            expectAndConsume(TokenType.KEYWORD_or);
            Condition right = parseBooleanTerm().asBoolean();
            expression = new BooleanParserExpression(new Or(expression.asBoolean(), right));
        }

        return expression;
    }

    private ParserExpression parseBooleanTerm() {
        ParserExpression expression = parseBooleanFactor();
        
        while (currentTokenType() == TokenType.KEYWORD_and) {
            expectAndConsume(TokenType.KEYWORD_and);
            Condition right = parseBooleanFactor().asBoolean();
            expression = new BooleanParserExpression(new And(expression.asBoolean(), right));
        }

        return expression;
    }

    private ParserExpression parseBooleanFactor() {
        return parseBooleanPrimary();
    }

    private ParserExpression parseBooleanPrimary() {
        if (consumeIfMatches(TokenType.KEYWORD_not)) {
            Condition expression = parseBooleanPrimary().asBoolean();
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

    private Condition parseIs(Expression left) {
        expectAndConsume(TokenType.KEYWORD_null);
        return new IsNull(left);
    }

    private Condition parseIn(Expression left) {
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
            /* What's the right wording for this?  The average SQL programmer
               shouldn't have to know what a "primary" is (it is sort
               of a made-up word devised for writing a grammar, really).
               Would "expression" work?
            */
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
        else if (number instanceof DecimalLiteral) {
            DecimalLiteral decimal = (DecimalLiteral) number;
            return new DecimalLiteral(decimal.value.negate(), location);
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

        abstract public Condition asBoolean();

    }

    public class NonBooleanParserExpression extends ParserExpression {

        private final Expression expression;

        public NonBooleanParserExpression(Expression expression) {
            this.expression = expression;
        }

        public Condition asBoolean() {
            throw new ParserException("expected boolean expression but got non-boolean expression");
        }

        public Expression asNonBoolean() {
            return expression;
        }

    }

    public class BooleanParserExpression extends ParserExpression {

        private final Condition expression;

        public BooleanParserExpression(Condition expression) {
            this.expression = expression;
        }

        public Condition asBoolean() {
            return expression;
        }

        public Expression asNonBoolean() {
            throw new ParserException("expected non-boolean expression but got boolean expression");
        }

    }

    private SingleColumn parseColumnReference() {
        Token firstIdentifier = expectAndConsume(TokenType.IDENTIFIER);

        if (consumeIfMatches(TokenType.PERIOD)) {
            Token column = expectAndConsume(TokenType.IDENTIFIER);
            return new SingleColumn(firstIdentifier.getText(), column.getText(),
                firstIdentifier.location.combine(column.location)
            );
        } else {
            return new SingleColumn(firstIdentifier.getText(), firstIdentifier.location);
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

        FromElement left = parseFromTable();
        while (true) {
            if (currentTokenType() == TokenType.KEYWORD_cross) {
                expectAndConsume(TokenType.KEYWORD_cross);
                expectAndConsume(TokenType.KEYWORD_join);
                FromElement right = parseFromItem();
                left = new InnerJoin(left, right, Condition.TRUE);
            }
            else if (currentTokenType() == TokenType.KEYWORD_inner) {
                expectAndConsume(TokenType.KEYWORD_inner);
                expectAndConsume(TokenType.KEYWORD_join);
                FromElement right = parseFromItem();
                expectAndConsume(TokenType.KEYWORD_on);
                Condition condition = parseWhere();
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
                Condition condition = parseWhere();
                left = new LeftJoin(left, right, condition);
            }
            else {
                return left;
            }
        }
    }

    public FromTable parseFromTable() {
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
    
    public UnresolvedTableReference parseTableReference() {
        String first = consumeIdentifier();
        if (consumeIfMatches(TokenType.PERIOD)) {
            String table = consumeIdentifier();
            return new UnresolvedTableReference(first, table);
        }
        else {
            return new UnresolvedTableReference(first);
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
