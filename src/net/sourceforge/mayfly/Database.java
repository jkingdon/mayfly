package net.sourceforge.mayfly;

import net.sf.jsqlparser.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.*;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.statement.drop.*;
import net.sf.jsqlparser.statement.insert.*;
import net.sf.jsqlparser.statement.select.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.jdbc.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.Select;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Database {

    class MyExpressionVisitor implements ExpressionVisitor {

        Column column;

        public void visit(NullValue nullValue) {
            throw new UnimplementedException();
        }

        public void visit(Function function) {
            throw new UnimplementedException();
        }

        public void visit(InverseExpression inverseExpression) {
            throw new UnimplementedException();
        }

        public void visit(JdbcParameter jdbcParameter) {
            throw new UnimplementedException();
        }

        public void visit(DoubleValue doubleValue) {
            throw new UnimplementedException();
        }

        public void visit(LongValue longValue) {
            throw new UnimplementedException();
        }

        public void visit(DateValue dateValue) {
            throw new UnimplementedException();
        }

        public void visit(TimeValue timeValue) {
            throw new UnimplementedException();
        }

        public void visit(TimestampValue timestampValue) {
            throw new UnimplementedException();
        }

        public void visit(Parenthesis parenthesis) {
            throw new UnimplementedException();
        }

        public void visit(StringValue stringValue) {
            throw new UnimplementedException();
        }

        public void visit(Addition addition) {
            throw new UnimplementedException();
        }

        public void visit(Division division) {
            throw new UnimplementedException();
        }

        public void visit(Multiplication multiplication) {
            throw new UnimplementedException();
        }

        public void visit(Subtraction subtraction) {
            throw new UnimplementedException();
        }

        public void visit(AndExpression andExpression) {
            throw new UnimplementedException();
        }

        public void visit(OrExpression orExpression) {
            throw new UnimplementedException();
        }

        public void visit(Between between) {
            throw new UnimplementedException();
        }

        public void visit(EqualsTo equalsTo) {
            throw new UnimplementedException();
        }

        public void visit(GreaterThan greaterThan) {
            throw new UnimplementedException();
        }

        public void visit(GreaterThanEquals greaterThanEquals) {
            throw new UnimplementedException();
        }

        public void visit(InExpression inExpression) {
            throw new UnimplementedException();
        }

        public void visit(IsNullExpression isNullExpression) {
            throw new UnimplementedException();
        }

        public void visit(LikeExpression likeExpression) {
            throw new UnimplementedException();
        }

        public void visit(MinorThan minorThan) {
            throw new UnimplementedException();
        }

        public void visit(MinorThanEquals minorThanEquals) {
            throw new UnimplementedException();
        }

        public void visit(NotEqualsTo notEqualsTo) {
            throw new UnimplementedException();
        }

        public void visit(Column tableColumn) {
            column = tableColumn;   
        }

        public void visit(SubSelect subSelect) {
            throw new UnimplementedException();
        }

        public void visit(CaseExpression caseExpression) {
            throw new UnimplementedException();
        }

        public void visit(WhenClause whenClause) {
            throw new UnimplementedException();
        }
    
    }

    class MySelectItemVisitor implements SelectItemVisitor {
        
        Expression expression;

        public void visit(AllColumns allColumns) {
            throw new UnimplementedException("no support for SELECT *");
        }

        public void visit(AllTableColumns allTableColumns) {
            throw new UnimplementedException("no support for SELECT Table.*");
        }

        public void visit(SelectExpressionItem selectExpressionItem) {
            expression = selectExpressionItem.getExpression();
        }

    }

    class MySelectVisitor implements SelectVisitor {
        String tableName;
        PlainSelect plainSelect;

        class MyFromVisitor implements FromItemVisitor {
            public void visit(Table table) {
                tableName = table.getName();
            }

            public void visit(SubSelect subSelect) {
                throw new UnimplementedException("Subselects not implemented");
            }
        }

        public void visit(PlainSelect plainSelect) {
            List fromItems = plainSelect.getFromItems();
            if (fromItems.size() != 1) {
                throw new UnimplementedException("only a single from currently supported in select");
            }
            FromItem fromItem = (FromItem) fromItems.get(0);
            MyFromVisitor fromVisitor = new MyFromVisitor();
            fromItem.accept(fromVisitor);
            
            this.plainSelect = plainSelect;
        }

        public void visit(Union union) {
            throw new UnimplementedException("Union selects not implemented");
        }
        
    }

    class MyItemListVisitor implements ItemsListVisitor {
        ExpressionList expressions;

        public void visit(SubSelect subSelect) {
            throw new UnimplementedException("no subselects yet");
        }

        public void visit(ExpressionList expressionList) {
            expressions = expressionList;
        }
    }

    private DataStore dataStore = new DataStore();

    /**
     * Execute an SQL command which does not return results.
     * This is similar to the JDBC {@link java.sql.Statement#executeUpdate(java.lang.String)}
     * but is more convenient if you have a Database instance around.
     * @return Number of rows changed.
     */
    public int execute(String command) throws SQLException {
        Statement statement = parse(command);
        if (statement instanceof Drop) {
            dataStore = dataStore.dropTable(((Drop)statement).getName());
            return 0;
        } else if (statement instanceof CreateTable) {
            CreateTable createTable = (CreateTable) statement;
            createTable(createTable.getTable().getName(), 
                    createTable.getColumnDefinitions());
            return 0;
        } else if (statement instanceof Insert) {
            Insert insert = (Insert) statement;
            return insert(insert.getTable().getName(), 
                    insert.getColumns(), insert.getItemsList());
        } else {
            throw new SQLException("unrecognized command for execute: " + command);
        }
    }

    /**
     * Execute an SQL command which does returns results.
     * This is similar to the JDBC {@link java.sql.Statement#executeQuery(java.lang.String)}
     * but is more convenient if you have a Database instance around.
     */
    public ResultSet query(String command) throws SQLException {
        Select select = Select.fromTree(Tree.parse(command));
        return select.select(dataStore);
    }

    private Statement parse(String command) throws SQLException {
        CCJSqlParserManager parser = new CCJSqlParserManager();
        try {
            return parser.parse(new StringReader(command));
        } catch (JSQLParserException e) {
            throw (SQLException) new SQLException("cannot parse " + command).initCause(e);
        }
    }

    private void createTable(String table, List columns) {
        dataStore = dataStore.createTable(table, columnNamesFromDefinitions(columns));
    }

    private List columnNamesFromDefinitions(List columns) {
        if (columns == null) {
            // CREATE TABLE FOO without any columns (is it even legal?)
            return Collections.EMPTY_LIST;
        }

        List columnNames = new ArrayList();
        for (Iterator iter = columns.iterator(); iter.hasNext(); ) {
            ColumnDefinition definition = (ColumnDefinition) iter.next();
            columnNames.add(definition.getColumnName());
        }
        return columnNames;
    }


    private int insert(String table, List columns, ItemsList itemsList)
    throws SQLException {
        List columnNames = new ArrayList();
        List values = new ArrayList();
        
        List items = walkList(itemsList);
        for (int i = 0; i < columns.size(); ++i) {
            Column column = (Column) columns.get(i);
            LongValue expression = (LongValue) items.get(i);
            columnNames.add(column.getColumnName());
            values.add(new Long(expression.getValue()));
        }
        
        dataStore = dataStore.addRow(table, columnNames, values);
        return 1;
    }

    private List walkList(ItemsList itemsList) {
        MyItemListVisitor visitor = new MyItemListVisitor();
        itemsList.accept(visitor);
        return visitor.expressions.getExpressions();
    }
    
    /**
     * Return table names.
     * 
     * Once this functionality is implemented in
     * {@link java.sql.DatabaseMetaData}, this method may go away or become
     * some kind of convenience method.
     */
    public Set tables() {
        return dataStore.tables();
    }

    /**
     * Column names in given table.
     * 
     * Once this functionality is implemented in
     * {@link java.sql.DatabaseMetaData}, this method may go away or become
     * some kind of convenience method.
     */
    public List columnNames(String tableName) throws SQLException {
        TableData tableData = dataStore.table(tableName);
        return tableData.columnNames();
    }

    /**
     * Number of rows in given table.
     * 
     * This is a convenience method.  Your production code will almost
     * surely be counting rows (if it needs to at all) via
     * {@link ResultSet} (or the SQL COUNT, once Mayfly implements it).
     * But this method may be convenient in tests.
     */
    public int rowCount(String tableName) throws SQLException {
        TableData tableData = dataStore.table(tableName);
        return tableData.rowCount();
    }

    /**
     * Get some data out. Probably just a temporary method until the
     * {@link ResultSet} code is done.
     */
    public int getInt(String tableName, String columnName, int rowIndex) throws SQLException {
        TableData tableData = dataStore.table(tableName);
        return tableData.getInt(columnName, rowIndex);
    }

    /**
     * Open a JDBC connection.
     * This is similar to the JDBC {@link DriverManager#getConnection(java.lang.String)}
     * but is more convenient if you have a Database instance around.
     */
    public Connection openConnection() throws SQLException {
        return new JdbcConnection(this);
    }

}
