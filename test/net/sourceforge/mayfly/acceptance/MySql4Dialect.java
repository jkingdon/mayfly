package net.sourceforge.mayfly.acceptance;


/**
 * This is for MySQL 4.x.  For 5.x, see 
 * {@link net.sourceforge.mayfly.acceptance.MySqlDialect}.
 * 
 * To make this work, see the instructions in
 * {@link net.sourceforge.mayfly.acceptance.MySqlDialect}.
 */
public class MySql4Dialect extends MySqlDialect {

    public boolean onIsRestrictedToJoinsTables() {
        return false;
    }
    
    public boolean considerTablesMentionedAfterJoin() {
        return true;
    }
    
    public boolean notBindsMoreTightlyThanIn() {
        return true;
    }
    
    public boolean aggregateDistinctIsForCountOnly() {
        return true;
    }
    
    public boolean columnInHavingMustAlsoBeInSelect() {
        return true;
    }
    
    public boolean rightHandArgumentToJoinCanBeJoin(boolean withParentheses) {
        return true;
    }
    
    public boolean canOrderByExpression(boolean isAggregate) {
        return !isAggregate;
    }

    public boolean allowTimestampInDateColumn() {
        return true;
    }

}
