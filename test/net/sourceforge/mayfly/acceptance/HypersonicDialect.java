package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HypersonicDialect extends Dialect {

    public Connection openConnection() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        return openAdditionalConnection();
    }

    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:mem:SqlTestCase");
    }

    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("SHUTDOWN", connection); // So next test gets a new database.
    }
    
    public boolean detectsAmbiguousColumns() {
        return false;
    }
    
    public boolean crossJoinRequiresOn() {
        // In hypersonic, CROSS JOIN seems to be a synonym for INNER JOIN
        return true;
    }
    
    public boolean crossJoinCanHaveOn() {
        return true;
    }
    
    public boolean onIsRestrictedToJoinsTables() {
        return false;
    }
    
    public boolean rightHandArgumentToJoinCanBeJoin(boolean withParentheses) {
        return false;
    }
    
    public boolean authorizationRequiredInCreateSchema() {
        return true;
    }

    public boolean requiresAllParameters() {
        return false;
    }
    
    public boolean orderByCountsAsWhat() {
        return true;
    }
    
    public boolean canOrderByExpression() {
        return true;
    }
    
    public boolean aggregateAsteriskIsForCountOnly() {
        // Hypersonic has a variety of behaviors, depending on whether there
        // are any rows, and which function.  None of them seem very useful.
        return false;
    }
    
    public boolean allowCountDistinctStar() {
        // What count(distinct *) means I don't really know.
        return true;
    }
    
    public boolean errorIfNotAggregateOrGroupedWhenGroupByExpression() {
        return false;
    }

    public boolean disallowColumnAndAggregateInExpression() {
        return false;
    }
    
    public boolean canHaveHavingWithoutGroupBy() {
        return true;
    }
    
    public boolean notRequiresBoolean() {
        return false;
    }
    
    public boolean disallowNullsInExpressions() {
        return false;
    }
    
    public boolean quotedIdentifiersAreCaseSensitive() {
        return true;
    }
    
    public boolean isReservedWord(String word) {
        return word.equalsIgnoreCase("if");
    }

    protected boolean constraintCanHaveForwardReference() {
        return false;
    }
    
    public boolean allowUniqueAsPartOfColumnDeclaration() {
        return false;
    }
    
    public boolean haveUpdateDefault() {
        return false;
    }
    
    public boolean willReadUncommitted() {
        return true;
    }
    
    public boolean foreignKeyCanReferToAnotherSchema() {
        return false;
    }
    
    public boolean haveTextType() {
        return false;
    }
    
    public boolean decimalScaleIsFromType() {
        return false;
    }
    
    public boolean canUpdateToDefault() {
        return false;
    }
    
    public boolean haveIdentity() {
        return true;
    }
    
    public boolean haveSql200xAutoIncrement() {
        return true;
    }
    
    public boolean autoIncrementIsRelativeToLastValue() {
        return true;
    }

}
