package net.sourceforge.mayfly.acceptance;


/**
 * This is for MySQL 4.x.  For 5.x, see 
 * {@link net.sourceforge.mayfly.acceptance.MySqlDialect}.
 * 
 * To make this work, see the instructions in
 * {@link net.sourceforge.mayfly.acceptance.MySqlDialect}.
 */
public class MySql4Dialect extends MySqlDialect {

    @Override
    public boolean onIsRestrictedToJoinsTables() {
        return false;
    }
    
    @Override
    public boolean considerTablesMentionedAfterJoin() {
        return true;
    }
    
    @Override
    public boolean notBindsMoreTightlyThanIn() {
        return true;
    }
    
    @Override
    public boolean aggregateDistinctIsForCountOnly() {
        return true;
    }
    
    @Override
    public boolean columnInHavingMustAlsoBeInSelect() {
        return true;
    }
    
    @Override
    public boolean rightHandArgumentToJoinCanBeJoin(boolean withParentheses) {
        return true;
    }
    
    @Override
    public boolean canOrderByExpression(boolean isAggregate) {
        return !isAggregate;
    }

    @Override
    public boolean allowTimestampInDateColumn() {
        return true;
    }

}
