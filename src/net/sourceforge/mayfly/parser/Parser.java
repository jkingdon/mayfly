package net.sourceforge.mayfly.parser;

import static net.sourceforge.mayfly.parser.TokenType.CLOSE_PAREN;
import static net.sourceforge.mayfly.parser.TokenType.EQUAL;
import static net.sourceforge.mayfly.parser.TokenType.IDENTIFIER;
import static net.sourceforge.mayfly.parser.TokenType.KEYWORD_character;
import static net.sourceforge.mayfly.parser.TokenType.KEYWORD_column;
import static net.sourceforge.mayfly.parser.TokenType.KEYWORD_like;
import static net.sourceforge.mayfly.parser.TokenType.KEYWORD_on;
import static net.sourceforge.mayfly.parser.TokenType.KEYWORD_set;
import static net.sourceforge.mayfly.parser.TokenType.KEYWORD_to;
import static net.sourceforge.mayfly.parser.TokenType.KEYWORD_values;
import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.BinaryCell;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Position;
import net.sourceforge.mayfly.datastore.constraint.Action;
import net.sourceforge.mayfly.datastore.constraint.Cascade;
import net.sourceforge.mayfly.datastore.constraint.NoAction;
import net.sourceforge.mayfly.datastore.constraint.SetDefault;
import net.sourceforge.mayfly.datastore.constraint.SetNull;
import net.sourceforge.mayfly.datastore.types.BinaryDataType;
import net.sourceforge.mayfly.datastore.types.DataType;
import net.sourceforge.mayfly.datastore.types.DateDataType;
import net.sourceforge.mayfly.datastore.types.DecimalDataType;
import net.sourceforge.mayfly.datastore.types.IntegerDataType;
import net.sourceforge.mayfly.datastore.types.StringDataType;
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
import net.sourceforge.mayfly.evaluation.command.ChangeColumn;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.CreateIndex;
import net.sourceforge.mayfly.evaluation.command.CreateSchema;
import net.sourceforge.mayfly.evaluation.command.CreateTable;
import net.sourceforge.mayfly.evaluation.command.Delete;
import net.sourceforge.mayfly.evaluation.command.DropColumn;
import net.sourceforge.mayfly.evaluation.command.DropConstraint;
import net.sourceforge.mayfly.evaluation.command.DropForeignKey;
import net.sourceforge.mayfly.evaluation.command.DropIndex;
import net.sourceforge.mayfly.evaluation.command.DropTable;
import net.sourceforge.mayfly.evaluation.command.Insert;
import net.sourceforge.mayfly.evaluation.command.LastIdentity;
import net.sourceforge.mayfly.evaluation.command.ModifyColumn;
import net.sourceforge.mayfly.evaluation.command.NoopCommand;
import net.sourceforge.mayfly.evaluation.command.RenameTable;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.SetSchema;
import net.sourceforge.mayfly.evaluation.command.SubselectedInsert;
import net.sourceforge.mayfly.evaluation.command.UnresolvedCheckConstraint;
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
import net.sourceforge.mayfly.evaluation.condition.LessEqual;
import net.sourceforge.mayfly.evaluation.condition.Like;
import net.sourceforge.mayfly.evaluation.condition.Not;
import net.sourceforge.mayfly.evaluation.condition.NotEqual;
import net.sourceforge.mayfly.evaluation.condition.Or;
import net.sourceforge.mayfly.evaluation.condition.SubselectedIn;
import net.sourceforge.mayfly.evaluation.expression.Average;
import net.sourceforge.mayfly.evaluation.expression.Concatenate;
import net.sourceforge.mayfly.evaluation.expression.Count;
import net.sourceforge.mayfly.evaluation.expression.CountAll;
import net.sourceforge.mayfly.evaluation.expression.CurrentTimestampExpression;
import net.sourceforge.mayfly.evaluation.expression.DefaultValue;
import net.sourceforge.mayfly.evaluation.expression.Divide;
import net.sourceforge.mayfly.evaluation.expression.Function;
import net.sourceforge.mayfly.evaluation.expression.Maximum;
import net.sourceforge.mayfly.evaluation.expression.Minimum;
import net.sourceforge.mayfly.evaluation.expression.Minus;
import net.sourceforge.mayfly.evaluation.expression.Multiply;
import net.sourceforge.mayfly.evaluation.expression.NullExpression;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.RealTimeSource;
import net.sourceforge.mayfly.evaluation.expression.ScalarSubselect;
import net.sourceforge.mayfly.evaluation.expression.SearchedCase;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.SpecifiedDefaultValue;
import net.sourceforge.mayfly.evaluation.expression.Sum;
import net.sourceforge.mayfly.evaluation.expression.TimeSource;
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

import java.io.Reader;
import java.util.ArrayList;
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

    private static final ResultRow PSEUDO_ROW_FOR_VALUE_CONSTRUCTOR = 
        new ResultRow() {
            @Override
            public SingleColumn findColumn(
                String tableOrAlias, String originalTableOrAlias,
                String columnName,
                Location location) {
                throw new MayflyException(
                    "values clause may not refer to column: " 
                    + Column.displayName(originalTableOrAlias, columnName),
                    location
                );
            }
        };

    private List tokens;
    private Token currentToken;

    private final boolean allowParameters;
    
    private final TimeSource timeSource;
    private final Options options;

    public Parser(String sql) {
        this(new Lexer(sql).tokens());
    }
    
    /**
     * Create a parser which reads input from a Reader.
     * The caller is responsible for closing the Reader.
     */
    public Parser(Reader sql, Options options) {
        this(new Lexer(sql).tokens(), false, new RealTimeSource(), options);
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
        this(tokens, allowParameters, new RealTimeSource());
    }

    public Parser(List tokens, boolean allowParameters, TimeSource timeSource) {
        this(tokens, allowParameters, timeSource, new Options());
    }

    public Parser(List tokens, boolean allowParameters, 
        TimeSource timeSource, Options options) {
        this.tokens = tokens;
        this.currentToken = tokens.isEmpty() ? null : (Token) tokens.get(0);
        this.allowParameters = allowParameters;
        this.timeSource = timeSource;
        this.options = options;
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
                if (currentToken.type != TokenType.END_OF_FILE
                    && currentToken.type != TokenType.SEMICOLON) {
                    throw new ParserException("end of command", currentToken);
                }
            }
        }
    }

    public Command parse() {
        Command command = parseCommand();
        expectAndConsume(TokenType.END_OF_FILE);
        return command;
    }

    Command parseCommand() {
        if (currentToken.type == TokenType.KEYWORD_select) {
            return parseSelect();
        }
        else if (consumeNonReservedWordIfMatches("call")) {
            return parseCall();
        }
        else if (consumeIfMatches(TokenType.KEYWORD_drop)) {
            if (consumeIfMatches(TokenType.KEYWORD_table)) {
                return parseDropTable();
            }
            else if (consumeIfMatches(TokenType.KEYWORD_index)) {
                return parseDropIndex();
            }
            else {
                throw new ParserException("drop command",
                    currentToken);
            }
        }
        else if (consumeIfMatches(TokenType.KEYWORD_create)) {
            if (consumeIfMatches(TokenType.KEYWORD_schema)) {
                return parseCreateSchema();
            }
            else if (consumeIfMatches(TokenType.KEYWORD_table)) {
                return parseCreateTable();
            }
            else if (consumeIfMatches(TokenType.KEYWORD_index)) {
                return parseCreateIndex(false);
            }
            else if (consumeIfMatches(TokenType.KEYWORD_unique)) {
                expectAndConsume(TokenType.KEYWORD_index);
                return parseCreateIndex(true);
            }
            else {
                throw new ParserException("create command",
                    currentToken);
            }
        }
        else if (consumeIfMatches(TokenType.KEYWORD_alter)) {
            if (consumeIfMatches(TokenType.KEYWORD_table)) {
                return parseAlterTable();
            }
            else if (consumeIfMatches(TokenType.KEYWORD_schema)) {
                return parseAlterSchema();
            }
            else {
                throw new ParserException("alter command", currentToken);
            }
        }
        else if (currentToken.type == KEYWORD_set) {
            return parseSetSchema();
        }
        else if (currentToken.type == TokenType.KEYWORD_insert) {
            return parseInsert();
        }
        else if (currentToken.type == TokenType.KEYWORD_update) {
            return parseUpdate();
        }
        else if (currentToken.type == TokenType.KEYWORD_delete) {
            return parseDelete();
        }
        else {
            throw new ParserException("command", currentToken);
        }
    }

    private Command parseCreateIndex(boolean unique) {
        String indexName = consumeIdentifier();
        expectAndConsume(TokenType.KEYWORD_on);
        UnresolvedTableReference table = parseTableReference();
        ColumnNames columns = parseColumnNamesForIndex();

        return new CreateIndex(table, indexName, columns, unique);
    }

    private Command parseCall() {
        expectNonReservedWord("identity");
        expectAndConsume(TokenType.OPEN_PAREN);
        expectAndConsume(CLOSE_PAREN);
        return new LastIdentity();
    }

    private Command parseSetSchema() {
        expectAndConsume(KEYWORD_set);
        expectAndConsume(TokenType.KEYWORD_schema);
        return new SetSchema(consumeIdentifier());
    }

    private Command parseInsert() {
        Location start = currentToken.location;

        expectAndConsume(TokenType.KEYWORD_insert);
        expectAndConsume(TokenType.KEYWORD_into);
        UnresolvedTableReference table = parseTableReference();
        
        if (consumeIfMatches(KEYWORD_set)) {
            List names = new ArrayList();
            ValueList values = new ValueList(currentToken.location);
            do {
                names.add(consumeIdentifier());
                expectAndConsume(EQUAL);
                values = values.with(parseAndEvaluate());
            } while (consumeIfMatches(TokenType.COMMA));
            return new Insert(table, new ImmutableList(names), values, 
                start.combine(values.location));
        }

        ImmutableList columnNames = parseColumnNamesForInsert();
        
        if (currentToken.type == TokenType.KEYWORD_select) {
            Select subselect = parseSelect();
            return new SubselectedInsert(table, columnNames, subselect);
        }
        else if (currentToken.type == KEYWORD_values) {
            ValueList values = parseValueConstructor();
    
            return new Insert(table, columnNames, values, start.combine(values.location));
        }
        else {
            throw new ParserException("VALUES or SELECT", currentToken);
        }
    }

    private Command parseUpdate() {
        expectAndConsume(TokenType.KEYWORD_update);
        UnresolvedTableReference table = parseTableReference();
        expectAndConsume(KEYWORD_set);
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
        expectAndConsume(EQUAL);
        Expression value = parseExpressionOrNull();
        return new SetClause(column, value);
    }

    private ImmutableList parseColumnNamesForInsert() {
        if (currentToken.type == TokenType.OPEN_PAREN) {
            expectAndConsume(TokenType.OPEN_PAREN);
            
            List columnNames = new ArrayList();
            if (currentToken.type != CLOSE_PAREN) {
                do {
                    columnNames.add(consumeIdentifier());
                } while (consumeIfMatches(TokenType.COMMA));
            }
            
            expectAndConsume(CLOSE_PAREN);
            return new ImmutableList(columnNames);
        }
        else {
            return null;
        }
    }

    private ColumnNames parseColumnNamesForIndex() {
        expectAndConsume(TokenType.OPEN_PAREN);

        List columnNames = new ArrayList();
        do {
            columnNames.add(consumeIdentifier());
            if (consumeIfMatches(TokenType.OPEN_PAREN)) {
                expectAndConsume(TokenType.NUMBER);
                expectAndConsume(CLOSE_PAREN);
            }
        } while (consumeIfMatches(TokenType.COMMA));

        expectAndConsume(CLOSE_PAREN);
        return new ColumnNames(new ImmutableList(columnNames));
    }
    
    private ColumnNames parseColumnNames() {
        expectAndConsume(TokenType.OPEN_PAREN);

        List columnNames = new ArrayList();
        do {
            columnNames.add(consumeIdentifier());
        } while (consumeIfMatches(TokenType.COMMA));

        expectAndConsume(CLOSE_PAREN);
        return new ColumnNames(new ImmutableList(columnNames));
    }
    
    ValueList parseValueConstructor() {
        Location start = expectAndConsume(TokenType.KEYWORD_values).location;

        ValueList values = new ValueList(start);
        expectAndConsume(TokenType.OPEN_PAREN);

        if (currentToken.type != CLOSE_PAREN) {
            do {
                values = values.with(parseAndEvaluate());
            } while (consumeIfMatches(TokenType.COMMA));
        }
        Location end = expectAndConsume(CLOSE_PAREN).location;
        return values.with(end);
    }

    private Value parseAndEvaluate() {
        Expression expression = parseExpressionOrNull();
        if (expression == null) {
            // default value
            return new Value(null, Location.UNKNOWN);
        }
        else {
            Cell cell = expression.evaluate(
                PSEUDO_ROW_FOR_VALUE_CONSTRUCTOR);
            return new Value(cell, expression.location);
        }
    }

    Expression parseExpressionOrNull() {
        Location start = currentToken.location;

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
                throw new MayflyException(
                    "Specify a null literal rather than an expression containing one",
                    start.combine(e.location()));
            }
        }
    }

    private Command parseDropTable() {
        boolean ifExists = false;
        if (consumeIfMatches(TokenType.KEYWORD_if)) {
            expectAndConsume(TokenType.KEYWORD_exists);
            ifExists = true;
        }
        UnresolvedTableReference table = parseTableReference();
        if (consumeIfMatches(TokenType.KEYWORD_if)) {
            expectAndConsume(TokenType.KEYWORD_exists);
            ifExists = true;
        }
        return new DropTable(table, ifExists);
    }
    
    private Command parseDropIndex() {
        String indexName = consumeIdentifier();
        if (consumeIfMatches(KEYWORD_on)) {
            UnresolvedTableReference table = parseTableReference();
            return new DropIndex(table, indexName);
        }
        else {
            return new DropIndex(null, indexName);
        }
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
    
    private Command parseAlterSchema() {
        consumeIdentifier();
        expectAndConsume(TokenType.KEYWORD_character);
        expectAndConsume(TokenType.KEYWORD_set);
        consumeIdentifier();
        return new NoopCommand();
    }

    private CreateTable parseCreateTable() {
        String tableName = consumeIdentifier();
        CreateTable table = new CreateTable(tableName);
        expectAndConsume(TokenType.OPEN_PAREN);

        do {
            parseTableElement(table);
        } while (consumeIfMatches(TokenType.COMMA));

        expectAndConsume(CLOSE_PAREN);
        
        parseTableTypeIfPresent();
        parseCharacterSetIfPresent();

        return table;
    }

    private void parseTableTypeIfPresent() {
        if (consumeNonReservedWordIfMatches("engine")) {
            expectAndConsume(EQUAL);
            Token token = expectAndConsume(IDENTIFIER);
            String tableType = token.getText();
            if ("innodb".equalsIgnoreCase(tableType)
                || "myisam".equalsIgnoreCase(tableType)) {
                // For now, ignore the type
            }
            else {
                throw new ParserException("unrecognized table type " + tableType,
                    token.location);
            }
        }
    }

    private void parseCharacterSetIfPresent() {
        if (consumeIfMatches(KEYWORD_character)) {
            expectAndConsume(KEYWORD_set);
            consumeIdentifier();
        }
    }

    private Command parseAlterTable() {
        UnresolvedTableReference table = parseTableReference();
        if (consumeIfMatches(TokenType.KEYWORD_drop)) {
            // optional according to SQL92 but does anyone omit it?
            if (consumeIfMatches(KEYWORD_column)) {
                String column = consumeIdentifier();
                return new DropColumn(table, column);
            }
            else if (consumeIfMatches(TokenType.KEYWORD_foreign)) {
                expectAndConsume(TokenType.KEYWORD_key);
                String constraintName = consumeIdentifier();
                return new DropForeignKey(table, constraintName);
            }
            else if (consumeIfMatches(TokenType.KEYWORD_constraint)) {
                String constraintName = consumeIdentifier();
                return new DropConstraint(table, constraintName);
            }
            else {
                throw new ParserException(
                    "alter table drop action", currentToken);
            }
        }
        else if (consumeIfMatches(TokenType.KEYWORD_add)) {
            // optional according to SQL92 but does anyone omit it?
            if (consumeIfMatches(KEYWORD_column)) {
                return parseAddColumn(table);
            }
            else if (lookingAtConstraint()) {
                UnresolvedConstraint key = parseConstraint();
                return new AddConstraint(table, key);
            }
            else {
                throw new ParserException(
                    "alter table add action", currentToken);
            }
        }
        else if (consumeNonReservedWordIfMatches("modify")) {
            expectAndConsume(KEYWORD_column);
            Column newColumn = parseColumnDisallowingMostConstraints(table);
            return new ModifyColumn(table, newColumn);
        }
        else if (consumeNonReservedWordIfMatches("change")) {
            expectAndConsume(KEYWORD_column);
            String oldName = consumeIdentifier();
            Column newColumn = parseColumnDisallowingMostConstraints(table);
            return new ChangeColumn(table, oldName, newColumn);
        }
        else if (matchesNonReservedWord("engine")) {
            parseTableTypeIfPresent();
            return new NoopCommand();
        }
        else if (consumeNonReservedWordIfMatches("rename")) {
            expectAndConsume(KEYWORD_to);
            String newName = consumeIdentifier();
            return new RenameTable(table, newName);
        }
        else if (currentToken.type == KEYWORD_character) {
            parseCharacterSetIfPresent();
            return new NoopCommand();
        }
        else {
            throw new ParserException("alter table action", currentToken);
        }
    }

    private Command parseAddColumn(UnresolvedTableReference table) {
        Position position = Position.LAST;
        Column newColumn = parseColumnDisallowingMostConstraints(table);
        if (consumeNonReservedWordIfMatches("after")) {
            Token column = expectAndConsume(IDENTIFIER);
            position = Position.after(column.getText(), column.location);
        }
        else if (consumeNonReservedWordIfMatches("first")) {
            /* first is a keyword in SQL92, but I guess we'll make
               it one here when we need to. */
            position = Position.FIRST;
        }
        return new AddColumn(table, newColumn, position);
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
        if (currentToken.type == IDENTIFIER) {
            table.addColumn(parseColumnDefinition(table));
        }
        else if (lookingAtConstraint()) {
            table.addConstraint(parseConstraint());
        }
        else if (consumeIfMatches(TokenType.KEYWORD_index)) {
            String name;
            if (currentToken.type == IDENTIFIER) {
                name = consumeIdentifier();
            }
            else {
                name = null;
            }
            ColumnNames columns = parseColumnNames();
            table.addIndex(name, columns);
        }
        else {
            throw new ParserException(
                "column or table constraint",
                currentToken);
        }
    }

    private boolean lookingAtConstraint() {
        return currentToken.type == TokenType.KEYWORD_primary
            || currentToken.type == TokenType.KEYWORD_unique
            || currentToken.type == TokenType.KEYWORD_foreign
            || currentToken.type == TokenType.KEYWORD_constraint
            || currentToken.type == TokenType.KEYWORD_check;
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
        else if (currentToken.type == TokenType.KEYWORD_foreign) {
            return parseForeignKeyConstraint(constraintName);
        }
        else if (consumeIfMatches(TokenType.KEYWORD_check)) {
            expectAndConsume(TokenType.OPEN_PAREN);
            Condition condition = parseCondition().asBoolean();
            expectAndConsume(CLOSE_PAREN);
            return new UnresolvedCheckConstraint(condition, constraintName);
        }
        else {
            throw new MayflyInternalException(
                "expected constraint but got " + currentToken.describe());
        }
    }

    private UnresolvedForeignKey parseForeignKeyConstraint(String constraintName) {
        Location start = currentToken.location;

        expectAndConsume(TokenType.KEYWORD_foreign);
        expectAndConsume(TokenType.KEYWORD_key);
        expectAndConsume(TokenType.OPEN_PAREN);
        String referencingColumn = consumeIdentifier();
        expectAndConsume(CLOSE_PAREN);

        expectAndConsume(TokenType.KEYWORD_references);
        UnresolvedTableReference targetTable = parseTableReference();
        expectAndConsume(TokenType.OPEN_PAREN);
        String targetColumn = consumeIdentifier();
        Token end = expectAndConsume(CLOSE_PAREN);
        
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
                    currentToken);
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
        else if (consumeIfMatches(KEYWORD_set)) {
            if (consumeIfMatches(TokenType.KEYWORD_null)) {
                return new SetNull();
            }
            else if (consumeIfMatches(TokenType.KEYWORD_default)) {
                return new SetDefault();
            }
            else {
                throw new ParserException("expected ON DELETE action " +
                    " but got SET " + currentToken.describe(),
                    currentToken.location);
            }
        }
        else {
            throw new ParserException("ON DELETE action",
                currentToken);
        }
    }

    Column parseColumnDefinition(CreateTable table) {
        String name = consumeIdentifier();
        ParsedDataType parsed = parseDataType();
        boolean isAutoIncrement = parsed.isAutoIncrement;
        boolean isSequence = parsed.isSequence;

        DefaultValue defaultValue = parseDefaultClause(name);
        
        Expression onUpdateValue = parseOnUpdateValue(name);

        if (currentToken.type == IDENTIFIER) {
            String text = currentToken.getText();
            if (text.equalsIgnoreCase("auto_increment")) {
                consumeIdentifier();
                isAutoIncrement = true;
            }
            else if (text.equalsIgnoreCase("generated")) {
                consumeIdentifier();
                expectAndConsume(TokenType.KEYWORD_by);
                expectAndConsume(TokenType.KEYWORD_default);
                expectAndConsume(TokenType.KEYWORD_as);
                expectIdentifier("identity");
                if (consumeIfMatches(TokenType.OPEN_PAREN)) {
                    expectIdentifier("start");
                    expectAndConsume(TokenType.KEYWORD_with);
                    defaultValue = 
                        new SpecifiedDefaultValue(parseDefaultValue(name));
                    expectAndConsume(CLOSE_PAREN);
                }
                isSequence = true;
            }
            else {
                // Need to leave the identifier unconsumed, as it
                // might be AFTER or FIRST from an ALTER TABLE
            }
        }

        if ((isAutoIncrement || isSequence) && !(defaultValue.isSpecified())) {
            defaultValue = new SpecifiedDefaultValue(new LongCell(1));
        }

        boolean isNotNull = parseColumnConstraints(table, name);

        return new Column(name, defaultValue, onUpdateValue, 
            isAutoIncrement, isSequence,
            parsed.type, isNotNull);
    }

    private DefaultValue parseDefaultClause(String name) {
        if (consumeIfMatches(TokenType.KEYWORD_default)) {
            return new SpecifiedDefaultValue(parseDefaultValue(name));
        }
        else {
            return DefaultValue.NOT_SPECIFIED;
        }
    }
    
    private Expression parseOnUpdateValue(String columnName) {
        if (consumeIfMatches(TokenType.KEYWORD_on)) {
            expectAndConsume(TokenType.KEYWORD_update);
            return parseDefaultValue(columnName);
        }
        else {
            return null;
        }
    }

    Expression parseDefaultValue(String columnName) {
        Location start = currentToken.location;

        if (currentToken.type == TokenType.NUMBER || 
            currentToken.type == TokenType.PERIOD) {
            return parseNumber(start).asNonBoolean();
        }
        else if (consumeIfMatches(TokenType.PLUS)) {
            return parseNumber(start).asNonBoolean();
        }
        else if (consumeIfMatches(TokenType.MINUS)) {
            return parseNegativeNumber(start).asNonBoolean();
        }
        else if (currentToken.type == TokenType.QUOTED_STRING) {
            return parseQuotedString().asNonBoolean();
        }
        else if (consumeIfMatches(TokenType.KEYWORD_null)) {
            return new NullExpression(start);
        }
        else if (consumeIfMatches(TokenType.KEYWORD_current_timestamp)) {
            return new CurrentTimestampExpression(start, timeSource);
        }
        else {
            throw new ParserException(
                "default value for column " + columnName,
                currentToken);
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
        boolean isSequence = false;
        DataType type;
        if (consumeIfMatches(TokenType.KEYWORD_integer)
            || consumeIfMatches(TokenType.KEYWORD_int)) {
            type = new IntegerDataType(TokenType.KEYWORD_integer.description());
        }
        else if (consumeIfMatches(TokenType.KEYWORD_smallint)) {
            type = new IntegerDataType(TokenType.KEYWORD_smallint.description());
        }
        else if (consumeIfMatches(TokenType.KEYWORD_varchar)) {
            expectAndConsume(TokenType.OPEN_PAREN);
            long size = consumeLong();
            expectAndConsume(CLOSE_PAREN);
            type = new StringDataType(
                TokenType.KEYWORD_varchar.description() +
                "(" +
                size +
                ")");
        }
        else if (consumeIfMatches(TokenType.KEYWORD_decimal)) {
            expectAndConsume(TokenType.OPEN_PAREN);
            int precision = consumeInteger();
            expectAndConsume(TokenType.COMMA);
            int scale = consumeInteger();
            expectAndConsume(CLOSE_PAREN);
            type = new DecimalDataType(precision, scale);
        }
        else if (currentToken.type == IDENTIFIER) {
            // These shouldn't be reserved if they are not in the
            // SQL standard, seems like.
            Token token = expectAndConsume(IDENTIFIER);
            String currentText = token.getText();
            if (currentText.equalsIgnoreCase("tinyint")) {
                type = new IntegerDataType("TINYINT");
            }
            else if (currentText.equalsIgnoreCase("bigint")) {
                type = new IntegerDataType("BIGINT");
            }
            else if (currentText.equalsIgnoreCase("text")) {
                type = new StringDataType("TEXT");
            }
            else if (currentText.equalsIgnoreCase("blob")) {
                if (consumeIfMatches(TokenType.OPEN_PAREN)) {
                    long size = consumeLong();
                    //expectAndConsume(TokenType.NUMBER);
                    expectAndConsume(CLOSE_PAREN);
                    type = new BinaryDataType(size);
                }
                else {
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
                type = new IntegerDataType("INTEGER");
            }
            else if (currentText.equalsIgnoreCase("serial")) {
                isSequence = true;
                type = new IntegerDataType("INTEGER");
            }
            else {
                throw new ParserException("data type", token);
            }
        }
        else {
            throw new ParserException("data type", currentToken);
        }
        return new ParsedDataType(isAutoIncrement, isSequence, type);
    }
    
    class ParsedDataType {
        final boolean isAutoIncrement;
        final boolean isSequence;
        final DataType type;

        public ParsedDataType(boolean isAutoIncrement, boolean isSequence, 
            DataType type) {
            this.isAutoIncrement = isAutoIncrement;
            this.isSequence = isSequence;
            this.type = type;
        }
        
    }

    Select parseSelect() {
        Location start = currentToken.location;
        
        expectAndConsume(TokenType.KEYWORD_select);
        
        boolean distinct;
        if (consumeIfMatches(TokenType.KEYWORD_all)) {
            distinct = false;
        }
        else if (consumeIfMatches(TokenType.KEYWORD_distinct)) {
            distinct = true;
        }
        else {
            distinct = false;
        }

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

        return new Select(what, from, where, groupBy, distinct, orderBy, limit,
            start);
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
            return new What(new All());
        }

        What what = new What();
        
        do {
            what = what.with(parseWhatElement());
        } while (consumeIfMatches(TokenType.COMMA));
        
        return what;
    }

    public WhatElement parseWhatElement() {
        if (currentToken.type == IDENTIFIER
            && ((Token) tokens.get(1)).type == TokenType.PERIOD
            && ((Token) tokens.get(2)).type == TokenType.ASTERISK) {

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
        while (currentToken.type == TokenType.MINUS
            || currentToken.type == TokenType.PLUS
            ) {
            Token token = consume();
            if (token.type == TokenType.MINUS) {
                left = new NonBooleanParserExpression(new Minus(left.asNonBoolean(), parseFactor().asNonBoolean()));
            }
            else if (token.type == TokenType.PLUS) {
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
        while (currentToken.type == TokenType.CONCATENATE
            || currentToken.type == TokenType.DIVIDE
            || currentToken.type == TokenType.ASTERISK
            ) {
            Token token = consume();
            if (token.type == TokenType.CONCATENATE) {
                left = new NonBooleanParserExpression(
                    new Concatenate(left.asNonBoolean(), parsePrimary().asNonBoolean())
                );
            }
            else if (token.type == TokenType.DIVIDE) {
                left = new NonBooleanParserExpression(
                    new Divide(left.asNonBoolean(), parsePrimary().asNonBoolean())
                );
            }
            else if (token.type == TokenType.ASTERISK) {
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
        
        while (currentToken.type == TokenType.KEYWORD_or) {
            expectAndConsume(TokenType.KEYWORD_or);
            Condition right = parseBooleanTerm().asBoolean();
            expression = new BooleanParserExpression(new Or(expression.asBoolean(), right));
        }

        return expression;
    }

    private ParserExpression parseBooleanTerm() {
        ParserExpression expression = parseBooleanFactor();
        
        while (currentToken.type == TokenType.KEYWORD_and) {
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
        if (consumeIfMatches(EQUAL)) {
            Expression right = parsePrimary().asNonBoolean();
            return new BooleanParserExpression(new Equal(left.asNonBoolean(), right));
        }
        if (currentToken.type == TokenType.KEYWORD_like) {
            return new BooleanParserExpression(parseLike(left));
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
        else if (consumeIfMatches(TokenType.LESS_EQUAL)) {
            Expression right = parsePrimary().asNonBoolean();
            return new BooleanParserExpression(
                new LessEqual(left.asNonBoolean(), right));
        }
        else if (consumeIfMatches(TokenType.GREATER_EQUAL)) {
            Expression right = parsePrimary().asNonBoolean();
            return new BooleanParserExpression(
                new LessEqual(right, left.asNonBoolean()));
        }
        else if (consumeIfMatches(TokenType.KEYWORD_not)) {
            return parseNotOperator(left);
        }
        else if (currentToken.type == TokenType.KEYWORD_in) {
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

    private Like parseLike(ParserExpression left) {
        expectAndConsume(KEYWORD_like);
        Expression right = parsePrimary().asNonBoolean();
        return new Like(left.asNonBoolean(), right);
    }

    /*
     * x NOT IN y, x NOT LIKE y, etc.
     */
    private BooleanParserExpression parseNotOperator(ParserExpression left) {
        if (currentToken.type == TokenType.KEYWORD_in) {
            return new BooleanParserExpression(new Not(parseIn(left.asNonBoolean())));
        }
        else if (currentToken.type == KEYWORD_like) {
            return new BooleanParserExpression(new Not(parseLike(left)));
        }
        else {
            throw new ParserException("IN or LIKE", currentToken);
        }
    }

    private Condition parseIs(Expression left) {
        expectAndConsume(TokenType.KEYWORD_null);
        return new IsNull(left);
    }

    private Condition parseIn(Expression left) {
        Condition result;

        expectAndConsume(TokenType.KEYWORD_in);
        expectAndConsume(TokenType.OPEN_PAREN);
        if (currentToken.type == TokenType.KEYWORD_select) {
            Select subselect = parseSelect();
            result = new SubselectedIn(left, subselect);
        }
        else {
            ImmutableList<Expression> expressions = parseExpressionList();
            result = new In(left, expressions);
        }
        expectAndConsume(CLOSE_PAREN);
        return result;
    }

    private ImmutableList<Expression> parseExpressionList() {
        List<Expression> expressions = new ArrayList<Expression>();
        do {
            expressions.add(parseExpression().asNonBoolean());
        } while (consumeIfMatches(TokenType.COMMA));
        return new ImmutableList<Expression>(expressions);
    }

    public ParserExpression parsePrimary() {
        AggregateArgumentParser argumentParser = new AggregateArgumentParser();
        Location start = currentToken.location;

        if (currentToken.type == IDENTIFIER) {
            return new NonBooleanParserExpression(parsePrimaryIdentifier());
        }
        else if (currentToken.type == TokenType.NUMBER || 
            currentToken.type == TokenType.PERIOD) {
            return parseNumber(start);
        }
        else if (consumeIfMatches(TokenType.PLUS)) {
            return parseNumber(start);
        }
        else if (consumeIfMatches(TokenType.MINUS)) {
            return parseNegativeNumber(start);
        }
        else if (currentToken.type == TokenType.KEYWORD_case) {
            return parseCase();
        }
        else if (currentToken.type == TokenType.QUOTED_STRING) {
            return parseQuotedString();
        }
        else if (currentToken.type == TokenType.PARAMETER) {
            return new NonBooleanParserExpression(
                new IntegerLiteral(parameterDummy()));
        }
        else if (consumeIfMatches(TokenType.KEYWORD_null)) {
            throw new FoundNullLiteral(start);
        }
        else if (currentToken.type == TokenType.BINARY) {
            Token token = expectAndConsume(TokenType.BINARY);
            return new NonBooleanParserExpression(
                new CellExpression(new BinaryCell(token.getBytes()), start));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_max, false)) {
            return new NonBooleanParserExpression(new Maximum(
                argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                argumentParser.location));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_min, false)) {
            return new NonBooleanParserExpression(new Minimum(
                argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                argumentParser.location));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_sum, false)) {
            return new NonBooleanParserExpression(new Sum(
                argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                argumentParser.location
            ));
        }
        else if (argumentParser.parse(TokenType.KEYWORD_avg, false)) {
            return new NonBooleanParserExpression(new Average(
                argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
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
                    argumentParser.expression, argumentParser.functionName, argumentParser.distinct,
                    argumentParser.location
                ));
            }
        }
        else if (consumeIfMatches(TokenType.OPEN_PAREN)) {
            ParserExpression expression;
            if (currentToken.type == TokenType.KEYWORD_select) {
                expression = parseScalarSubselect();
            }
            else {
                expression = parseCondition();
            }
            expectAndConsume(CLOSE_PAREN);
            return expression;
        }
        else {
            /* I'm pretty sure the distinction between "expression", 
               "primary", "term", etc, are just for the grammar - that
               users will be happy thinking of it all as expressions. 
               (these are basically concepts introduced so we can 
               write a grammar, not really the way that programmers 
               think of precedence).  */
            /* Do we use the word expression for both boolean and
               non-boolean? */
            throw new ParserException("expression", currentToken);
        }
    }

    private int parameterDummy() {
        if (allowParameters) {
            expectAndConsume(TokenType.PARAMETER);

            /* We are just checking the syntax, so this value won't
               be used anywhere */
            return 0;
        }
        else {
            throw new MayflyException(
                "Attempt to specify '?' outside a prepared statement",
                currentToken.location);
        }
    }

    private ParserExpression parseScalarSubselect() {
        return new NonBooleanParserExpression(
            new ScalarSubselect(parseSelect()));
    }

    private ParserExpression parseCase() {
        Location start = currentToken.location;

        SearchedCase result = new SearchedCase();
        expectAndConsume(TokenType.KEYWORD_case);
        do {
            expectAndConsume(TokenType.KEYWORD_when);
            Condition condition = parseWhere();
            expectAndConsume(TokenType.KEYWORD_then);
            
            // This will get more complicated when/if we allow NULL
            Expression thenValue = parseExpression().asNonBoolean();
            
            result = result.withCase(condition, thenValue);
        }
        while (currentToken.type == TokenType.KEYWORD_when);
        
        if (consumeIfMatches(TokenType.KEYWORD_else)) {
            result = result.withElse(parseExpression().asNonBoolean());
        }
        Token end = expectAndConsume(TokenType.KEYWORD_end);
        return new NonBooleanParserExpression(
            result.withLocation(start.combine(end.location))
        );
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
        if (currentToken.type == TokenType.PERIOD) {
            Token period = expectAndConsume(TokenType.PERIOD);
            if (currentToken.type == TokenType.NUMBER) {
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
            if (currentToken().type == aggregateTokenType) {
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
                Token end = expectAndConsume(CLOSE_PAREN);

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

        @Override
        public Condition asBoolean() {
            throw new ParserException(
                "expected boolean expression but got non-boolean expression",
                expression.location);
        }

        @Override
        public Expression asNonBoolean() {
            return expression;
        }

    }

    public class BooleanParserExpression extends ParserExpression {

        private final Condition condition;

        public BooleanParserExpression(Condition expression) {
            this.condition = expression;
        }

        @Override
        public Condition asBoolean() {
            return condition;
        }

        @Override
        public Expression asNonBoolean() {
            throw new ParserException(
                "expected non-boolean expression but got boolean expression");
        }

    }

    private Expression parsePrimaryIdentifier() {
        Token firstIdentifier = expectAndConsume(IDENTIFIER);

        if (consumeIfMatches(TokenType.PERIOD)) {
            Token column = expectAndConsume(IDENTIFIER);
            return new SingleColumn(firstIdentifier.getText(), column.getText(),
                firstIdentifier.location.combine(column.location),
                options
            );
        }
        else if (consumeIfMatches(TokenType.OPEN_PAREN)) {
            ImmutableList<Expression> arguments = parseExpressionList();
            Token close = expectAndConsume(CLOSE_PAREN);
            return Function.create(firstIdentifier.getText(), arguments,
                firstIdentifier.location.combine(close.location),
                options);
        }
        else {
            return new SingleColumn(firstIdentifier.getText(), 
                firstIdentifier.location, options);
        }
    }

    private SingleColumn parseColumnReference() {
        Expression expression = parseExpression().asNonBoolean();
        if (expression instanceof SingleColumn) {
            return (SingleColumn) expression;
        }
        else {
            throw new ParserException(
                "expected column reference in ORDER BY but got " + expression.displayName(),
                expression.location);
        }
    }

    public From parseFromItems() {
        From from = new From();
        do {
            from = from.with(parseFromItem());
        } while (consumeIfMatches(TokenType.COMMA));
        return from;
    }

    private FromElement parseFromItem() {
        FromElement left;
        if (consumeIfMatches(TokenType.OPEN_PAREN)) {
            left = parseFromItem();
            expectAndConsume(CLOSE_PAREN);
        }
        else {
            left = parseFromTable();
        }

        while (true) {
            if (consumeIfMatches(TokenType.KEYWORD_cross)) {
                expectAndConsume(TokenType.KEYWORD_join);
                FromElement right = parseFromItem();
                left = new InnerJoin(left, right, Condition.TRUE);
            }
            else if (consumeIfMatches(TokenType.KEYWORD_inner)) {
                expectAndConsume(TokenType.KEYWORD_join);
                FromElement right = parseFromItem();
                expectAndConsume(TokenType.KEYWORD_on);
                Condition condition = parseWhere();
                left = new InnerJoin(left, right, condition);
            }
            else if (consumeIfMatches(TokenType.KEYWORD_left)) {
                if (currentToken.type == TokenType.KEYWORD_outer) {
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
        if (currentToken.type == TokenType.PERIOD) {
            expectAndConsume(TokenType.PERIOD);
            table = consumeIdentifier();
        } else {
            table = firstIdentifier;
        }

        if (currentToken.type == IDENTIFIER) {
            String alias = consumeIdentifier();
            return new FromTable(table, alias);
        } else {
            return new FromTable(table);
        }
    }
    
    public UnresolvedTableReference parseTableReference() {
        Token first = expectAndConsume(IDENTIFIER);
        if (consumeIfMatches(TokenType.PERIOD)) {
            Token table = expectAndConsume(IDENTIFIER);
            return new UnresolvedTableReference(
                first.getText(), table.getText(), 
                first.location.combine(table.location),
                options);
        }
        else {
            return new UnresolvedTableReference(
                null, first.getText(), first.location, options);
        }
    }

    private Aggregator parseGroupBy() {
        if (consumeIfMatches(TokenType.KEYWORD_group)) {
            expectAndConsume(TokenType.KEYWORD_by);

            List<GroupItem> items = new ArrayList<GroupItem>();
            do {
                items.add(parseGroupItem());
            } while (consumeIfMatches(TokenType.COMMA));

            Condition having;
            if (consumeIfMatches(TokenType.KEYWORD_having)) {
                having = parseCondition().asBoolean();
            }
            else {
                having = Condition.TRUE;
            }
            return new GroupBy(new ImmutableList<GroupItem>(items), having);
        }
        else {
            if (currentToken.type == TokenType.KEYWORD_having) {
                throw new ParserException(
                    "can't specify HAVING without GROUP BY",
                    currentToken.location);
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
                orderBy = orderBy.with(parseOrderItem());
            } while (consumeIfMatches(TokenType.COMMA));
            return orderBy;
        }
        else {
            return new OrderBy();
        }
    }

    OrderItem parseOrderItem() {
        if (currentToken.type == TokenType.NUMBER) {
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

    Limit parseLimit() {
        if (currentToken.type == TokenType.KEYWORD_limit) {
            expectAndConsume(TokenType.KEYWORD_limit);
            int count = consumeIntegerOrParameter();
            
            if (consumeNonReservedWordIfMatches("offset")) {
                int offset = consumeIntegerOrParameter();
                return new Limit(count, offset);
            }
            
            return new Limit(count, Limit.NO_OFFSET);
        }
        else {
            return Limit.NONE;
        }
    }

    private int consumeIntegerOrParameter() {
        if (currentToken.type == TokenType.NUMBER) {
            return consumeInteger();
        }
        else if (currentToken.type == TokenType.PARAMETER) {
            return parameterDummy();
        }
        else {
            throw new ParserException("number", currentToken);
        }
    }

    public String remainingTokens() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator iter = tokens.iterator();
        while (iter.hasNext()) {
            Token token = (Token) iter.next();
            if (token.type == TokenType.END_OF_FILE) {
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
        Token token = expectAndConsume(IDENTIFIER);
        return token.getText();
    }

    private void expectIdentifier(String expectedIdentifier) {
        Token token = expectAndConsume(IDENTIFIER);
        String text = token.getText();
        if (!text.equalsIgnoreCase(expectedIdentifier)) {
            throw new ParserException(expectedIdentifier, token);
        }
    }

    int consumeInteger() {
        Token number = expectAndConsume(TokenType.NUMBER);
        String text = number.getText();
        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
            // Out of range.  Most (all?) other cases are prevented in the lexer.
            throw new ParserException(text + " is out of range",
                number.location);
        }
    }

    long consumeLong() {
        Token number = expectAndConsume(TokenType.NUMBER);
        String text = number.getText();
        try {
            return Long.parseLong(text);
        }
        catch (NumberFormatException e) {
            // Out of range.  Most (all?) other cases are prevented in the lexer.
            throw new ParserException(text + " is out of range",
                number.location);
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
        if (currentToken.type == type) {
            expectAndConsume(type);
            return true;
        }
        return false;
    }
    
    private boolean consumeNonReservedWordIfMatches(String word) {
        if (currentToken.type != IDENTIFIER) {
            return false;
        }

        String currentText = currentToken.getText();
        if (word.equalsIgnoreCase(currentText)) {
            expectAndConsume(IDENTIFIER);
            return true;
        }
        
        return false;
    }

    private boolean matchesNonReservedWord(String word) {
        if (currentToken.type != IDENTIFIER) {
            return false;
        }

        String currentText = currentToken.getText();
        if (word.equalsIgnoreCase(currentText)) {
            return true;
        }

        return false;
    }

    private void expectNonReservedWord(String word) {
        if (!consumeNonReservedWordIfMatches(word)) {
            throw new ParserException(word, currentToken);
        }
    }

    Token expectAndConsume(TokenType expectedType) {
        Token token = currentToken;
        if (token.type != expectedType) {
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
        Token consumedToken = (Token) tokens.remove(0);
        this.currentToken = tokens.isEmpty() ? null : (Token) tokens.get(0);
        return consumedToken;
    }
    
    Token currentToken() {
        return currentToken;
    }

    private String describeExpectation(TokenType expectedType) {
        return expectedType.description();
    }

}
