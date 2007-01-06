package net.sourceforge.mayfly.acceptance;

import net.sourceforge.mayfly.util.StringBuilder;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Dialect {

    abstract public Connection openConnection() throws Exception;
    abstract public Connection openAdditionalConnection() throws Exception;

    public void assertTableCount(int expected) {
        // Could probably do this with JDBC metadata or database-specific tricks.
        // Not clear we should bother.
    }

    abstract public void shutdown(Connection connection) throws Exception;

    public void assertMessage(String expectedMessage, SQLException exception) {
        // To assert on this we'd need to keep lists of messages for many
        // databases in many versions.  That seems hard.
        // But we would like to see that databases fail for the same
        // reasons.  So we provide the ability to manually inspect
        // the messages side by side.

        if (SqlTestCase.SHOW_MESSAGES) {
            System.out.print("Mayfly message would be " + expectedMessage + "\n");
            System.out.print("Actual message was " + exception.getMessage() + "\n\n");
        }
        else if (SqlTestCase.SHOW_STACK_TRACES) {
            System.out.print("Mayfly message would be " + expectedMessage + "\n");
            System.out.print("Actual exception was:\n");
            printException(exception);
            System.out.print("\n\n");
        }
    }

    public void assertMessage(String expectedMessage, SQLException exception, 
        int expectedStartLine, int expectedStartColumn, int expectedEndLine, int expectedEndColumn) {
        assertMessage(expectedMessage, exception);
    }

    private void printException(SQLException exception) {
        exception.printStackTrace(System.out);
        System.out.print("SQL state was: " + exception.getSQLState() + "\n");
        System.out.print("Vendor code was: " + exception.getErrorCode() + "\n");
        if (exception.getNextException() != null) {
            // Is it really true that next exceptions are not related to
            // causes?  Or is that just an artifact of libgcj 4.0.2?
            printException(exception.getNextException());
        }
    }

    public boolean backslashInAStringIsAnEscape() {
        // For most SQL dialects (including SQL92 I believe), '\' is just a string
        // with one character in it.
        
        /** Note that there are security considerations to this choice; an application
            trying to prevent SQL injection must use database-specific quoting
            (or, setString) */
        return false;
    }

    public boolean isReservedWord(String word) {
        // War on reserved words: they can make it quite a pain to
        // port SQL from one implementation to another.  Mayfly's
        // rule of thumb: Don't put big kluges in the parser to
        // avoid a reserved word, but make words non-reserved
        // where feasible.
        
        // A few specific cases:

        // LIMIT needs/wants to be a reserved word, but there is no
        // particular need for OFFSET to be (unless just for symmetry
        // with LIMIT or something).
        return false;
    }

    public boolean tableNamesMightBeCaseSensitive() {
        return false;
    }

    public boolean constraintNamesMightBeCaseSensitive() {
        return false;
    }

    public boolean detectsAmbiguousColumns() {
        return true;
    }

    /** 
     * @internal
     * Should a test look for behavior in which Mayfly intentionally diverges
     * from what other databases do.
     * 
     * (In most case it makes more sense to have an individual test for a specific questions
     * like detectsAmbiguousColumns or whatever).  */
    public boolean expectMayflyBehavior() {
        return false;
    }

    /**
     * @internal
     * This is how we mark things where Mayfly doesn't yet implement the behavior
     * which is desired for Mayfly.  So for non-Mayfly databases, there is no 
     * "wish" to be marked.
     */
    public boolean wishThisWereTrue() {
        return true;
    }
    
    public boolean crossJoinRequiresOn() {
        return false;
    }

    public boolean crossJoinCanHaveOn() {
        return false;
    }

    public boolean innerJoinRequiresOn() {
        return true;
    }
    
    public boolean rightHandArgumentToJoinCanBeJoin(boolean withParentheses) {
        return true;
    }

    public boolean onIsRestrictedToJoinsTables() {
        return true;
    }

    public boolean considerTablesMentionedAfterJoin() {
        return false;
    }
    
    boolean onCanMentionOutsideTable() {
        return !onIsRestrictedToJoinsTables();
    }

    public boolean allowDuplicateTableInQuery() {
        return false;
    }
    
    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return true;
    }

    public boolean requiresAllParameters() {
        return true;
    }

    public boolean stringComparisonsAreCaseInsensitive() {
        return false;
    }

    /**
     * @internal
     * The SQL Standard is said to specify false.
     */
    public boolean trailingSpacesConsultedInComparisons() {
        return false;
    }

    public boolean notBindsMoreTightlyThanIn() {
        return false;
    }

    public boolean orderByCountsAsWhat() {
        return false;
    }

    public boolean haveLimit() {
        // Interestingly enough, the majority of my test databases
        // do have this, in a more or less compatible fashion.
        return true;
    }

    public boolean canHaveLimitWithoutOrderBy() {
        return false;
    }

    public boolean fromIsOptional() {
        return false;
    }

    public boolean verticalBarsMeanConcatenation() {
        return true;
    }
    
    public boolean caseExpressionPickyAboutTypes() {
        return false;
    }

    public boolean maySpecifyTableDotColumnToJdbc() {
        return false;
    }

    public boolean schemasMissing() {
        return false;
    }

    public boolean canCreateSchemaAndTablesInSameStatement() {
        // Not sure how clean/useful/popular this is.
        return true;
    }
    
    public boolean authorizationAllowedInCreateSchema() {
        return true;
    }

    public boolean authorizationRequiredInCreateSchema() {
        return false;
    }

    public String createEmptySchemaCommand(String name) {
        StringBuilder sql = new StringBuilder();
        sql.append("create schema ");
        sql.append(name);
        if (authorizationRequiredInCreateSchema()) {
            sql.append(" authorization dba");
        }
        return sql.toString();
    }

    public boolean aggregateDistinctIsForCountOnly() {
        return false;
    }

    public boolean aggregateAsteriskIsForCountOnly() {
        return true;
    }

    public boolean canSumStrings(boolean rowsPresent) {
        return false;
    }

    public boolean errorIfNotAggregateOrGrouped() {
        return true;
    }

    public boolean errorIfNotAggregateOrGroupedWhenGroupByExpression() {
        return errorIfNotAggregateOrGrouped();
    }

    public boolean canGroupByExpression() {
        return true;
    }

    public boolean canOrderByExpression(boolean isAggregate) {
        return false;
    }

    public boolean canGetValueViaExpressionName() {
        return false;
    }

    public boolean allowCountDistinctStar() {
        return false;
    }

    public boolean canQuoteIdentifiers() {
        return true;
    }

    public boolean columnInHavingMustAlsoBeInSelect() {
        return false;
    }

    public boolean canHaveHavingWithoutGroupBy() {
        return false;
    }

    public boolean nullSortsLower() {
        // I don't know whether there are arguments pro or con on this.
        // Different databases seem to disagree, and several make it
        // configurable somehow.
        return true;
    }

    public boolean disallowColumnAndAggregateInExpression() {
        return true;
    }

    public boolean notRequiresBoolean() {
        return true;
    }

    public boolean numberOfValuesMustMatchNumberOfColumns() {
        return true;
    }

    public boolean valuesClauseCanReferToColumn() {
        return false;
    }

    /**
     * @internal
     * Does the syntax "insert into foo() values()" work?
     * This is an extension from standard SQL, but not having it
     * leads to craziness like null not meaning null in some
     * databases.
     * 
     * Should check Hibernate Dialect class - I think other databases
     * have this with a different syntax.
     */
    public boolean canInsertNoValues() {
        return true;
    }

    public boolean canConcatenateStringAndInteger() {
        // Most databases seem to allow this.  I'm sure there
        // are larger issues/tradeoffs here (like "do what I
        // mean" versus avoiding surprises).
        return true;
    }

    public boolean disallowNullsInExpressions() {
        // If this is true, insist people say "null" rather than
        // "5 + null".  This may reduce confusion over the
        // "null propagates up" semantics (or might just delay
        // the time when people discover them :-)).
        return true;
    }

    public boolean disallowNullOnRightHandSideOfIn() {
        return true;
    }

    public boolean canGetValueViaExpression() {
        return false;
    }

    protected boolean constraintCanHaveForwardReference() {
        return true;
    }

    public boolean duplicateConstraintNamesOk() {
        return false;
    }

    public boolean uniqueColumnMayBeNullable() {
        return true;
    }
    
    public boolean allowMultipleNullsInUniqueColumn() {
        /* Most databases which allow nulls in unique columns allow
           more than one null (which is what this setting means
           by true).
           (MySQL, Postgres, Oracle, Firebird, SQLlite)
           It isn't clear what null would mean if you only get 
           one row which gets to omit that columns.  
           
           On the other hand,
           false is analogous to the GROUP BY model in which all rows
           with null go in a single group.  (MS-SQL, Informix, Hypersonic).  
           
           Data from http://www.sqlite.org/nulls.html */
        return true;
    }

    public boolean allowUniqueAsPartOfColumnDeclaration() {
        return true;
    }

    public boolean haveUpdateDefault() {
        return true;
    }

    public boolean errorIfUpdateToAggregate(boolean rowsPresent) {
        return true;
    }
    
    public boolean errorIfAggregateInWhere() {
        return true;
    }

    public boolean haveOnUpdateValue() {
        return false;
    }

    public boolean quotedIdentifiersAreCaseSensitive() {
        // I guess SQL92 says this should be true.
        // Perhaps a bit tricky to get this true, and still have
        // messages case-preserving in general.
        return false;
    }

    public boolean haveSlashStarComments() {
        return true;
    }

    public boolean haveTransactions() {
        /**
         * @internal
         * Should we be testing {@link Connection#getTransactionIsolation()}
         * and {@link Connection#setTransactionIsolation(int)} ?
         * Need to do more testing, but the issue is whether most/any
         * databases will indicate what they can do, or change their
         * behavior based on this setting.  In a case like MySQL,
         * transaction capability is per-table, for one thing. 
         * 
         * Anyway, for now we set the isolation level appropriate for
         * each test, but in terms of figuring out what the database
         * can do, we have methods here.
         */
        
        return true;
    }

    public boolean willReadUncommitted() {
        return false;
    }

    public boolean canProvideRepeatableRead() {
        return true;
    }

    public boolean willWaitForWriterToCommit() {
        return false;
    }

    public boolean haveForUpdate() {
        return true;
    }

    public String tableTypeForTransactions() {
        return "";
    }
    
    public boolean autoCommitMustBeOffToCallRollback() {
        return true;
    }
    
    /**
       @internal
       
       Do whatever is needed before calling close() on the connection.
                 
       The whole thing, of what you need to do to the transaction
       before calling close(), might be worth considering, 
       writing tests for, and reading documentation for various
       databases.
    */
    public void endTransaction(Connection connection) throws SQLException {
        // By default, nothing is needed.
    }

    public String tableTypeForForeignKeys() {
        return "";
    }
    
    public boolean foreignKeyCanReferToAnotherSchema() {
        return true;
    }

    public boolean foreignKeyJustNeedsIndex() {
        return false;
    }

    public boolean onDeleteSetDefaultMissing(boolean tableCreateTime) {
        return false;
    }

    public boolean haveTinyint() {
        return true;
    }

    public boolean expressionsAreTypeLong() {
        return true;
    }
    
    public Class typeFromAddingLongs() {
        // Obvious question here is what about overflow?
        return Long.class;
    }
    
    public Class typeOfInteger() {
        return Integer.class;
    }

    public boolean allowHexForInteger() {
        return false;
    }

    public boolean allowHexForBinary() {
        return true;
    }

    public boolean haveTextType() {
        return true;
    }

    public String binaryTypeName() {
        return "blob(255)";
    }
    
    /** Is there a command DROP TABLE name IF EXISTS (with IF EXISTS after the name)? */
    public boolean haveDropTableFooIfExists() {
        return true;
    }

    /** Is there a command DROP TABLE IF EXISTS name (with IF EXISTS before the name)? */
    public boolean haveDropTableIfExistsFoo() {
        return true;
    }

    public boolean addingColumnCountsAsAffectedRow() {
        return false;
    }

    /**
     * Does adding a column with ALTER TABLE require there
     * to be a default value even if there are no rows?
     * True is kind of a bogus setting - there is no need
     * for such a default value to make the add column
     * work, and perhaps requiring a value to be
     * specified (that is, no default) is what is desired
     * for subsequent statements beyond the ALTER TABLE.
     */
    public boolean notNullRequiresDefault() {
        return false;
    }

    public boolean haveDropColumn() {
        return true;
    }

    public boolean haveDropForeignKey() {
        return true;
    }

    public boolean haveModifyColumn() {
        return true;
    }

    public boolean canDropLastColumn() {
        return false;
    }

    public boolean defaultValueCanBeExpression() {
        // Who does this besides postgres?

        // The postgres manual gives two examples: setting a date to now() (the
        // SQL92 way seems to be CURRENT_DATE/CURRENT_TIME/CURRENT_TIMESTAMP,
        // which is specifically allowed as a default value),
        
        // and auto-increment (but is the default-value-as-expression way of
        // doing it one that we want to try to have?).
        return false;
    }

    public boolean canUpdateToDefault() {
        return true;
    }

    public boolean allowJdbcParameterAsDefault() {
        return false;
    }

    public boolean notNullImpliesDefaults() {
        return false;
    }
    
    public boolean timestampDoesNotRespectNull() {
        return false;
    }

    public boolean allowDateInTimestampColumn() {
        return false;
    }

    public boolean allowTimestampInDateColumn() {
        return false;
    }

    public boolean haveAutoUnderbarIncrement() {
        return false;
    }

    public boolean haveSerial() {
        return false;
    }

    public boolean haveIdentity() {
        return false;
    }

    public boolean haveSql200xAutoIncrement() {
        return false;
    }
    
    /**
     * @internal
     * Return the preferred (or one that will work) way to specify
     * an auto-increment column, including the type.
     * 
     * Generally should also declare it a primary key.
     */
    public String identityType() {
        return "identity primary key";
    }
    
    public String lastIdentityValueQuery(String table, String column) {
        return "call identity()"; //hypersonic
    }

    public boolean autoIncrementIsRelativeToLastValue() {
        // Not sure what the arguments on either side of
        // this one are.  (If not relative to the last
        // inserted value, it is a sequence, which is
        // independent of what was explicitly inserted).
        return false;
    }

    public boolean decimalScaleIsFromType() {
        // False is just bugginess, as far as I know.
        return true;
    }

    public boolean datesAreOff() {
        return false;
    }

    /**
     * @internal
     * Kind of a catch-all.  There are also more specific ones like
     * {@link #canGetBytesOnNumber}.
     */
    public boolean dataTypesAreEnforced() {
        return true;
    }

    public boolean canGetBytesOnNumber() {
        return false;
    }

    /**
     * @internal
     * For cases not covered by {@link #canSetIntegerOnStringColumn()} or
     * other more specific cases.
     */
    public boolean canMixStringAndInteger() {
        return false;
    }

    public boolean canSetIntegerOnStringColumn() {
        return !expectMayflyBehavior();
    }

    public boolean canSetStringOnDecimalColumn() {
        return false;
    }

    public boolean onUpdateSetNullAndCascadeMissing() {
        return false;
    }

    /**
     * @internal
     * Slightly misnamed; it applies to the column alias cases
     * which Mayfly can't handle.  Things like {@link ResultTest#testAs()}
     * just use select a AS b unconditionally.
     */
    public boolean haveColumnAlias() {
        return wishThisWereTrue();
    }
    
    public boolean whereCanReferToColumnAlias() {
        return haveColumnAlias();
    }
    
    public boolean canGroupByColumnAlias() {
        return haveColumnAlias();
    }

    public boolean allowOrderByOnDelete() {
        return true;
    }

    public boolean deleteAllRowsIsSmartAboutForeignKeys() {
        return false;
    }

    public boolean errorIfOrderByNotInSelectDistinct() {
        return true;
    }

    public boolean metaDataExpectsUppercase() {
        return false;
    }
    
    public boolean metaDataProblemWithUppercaseTableName() {
        return false;
    }

    public boolean createTableCanContainIndex() {
        return false;
    }

}
