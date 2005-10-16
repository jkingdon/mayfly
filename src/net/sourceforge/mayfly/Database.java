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

import java.io.*;
import java.sql.*;
import java.util.*;

public class Database {

    class MyExpressionVisitor implements ExpressionVisitor{

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

    private Map tables = new HashMap();

    public void execute(String command) throws SQLException {
        Statement statement = parse(command);
        if (statement instanceof Drop) {
            dropTable(((Drop)statement).getName());
        } else if (statement instanceof CreateTable) {
            CreateTable createTable = (CreateTable) statement;
            createTable(createTable.getTable().getName(), 
                    createTable.getColumnDefinitions());
        } else if (statement instanceof Insert) {
            Insert insert = (Insert) statement;
            insert(insert.getTable().getName(), 
                    insert.getColumns(), insert.getItemsList());
        } else {
            throw new SQLException("unrecognized command for execute: " + command);
        }
    }

    public ResultSet query(String command) throws SQLException {
        Statement statement = parse(command);
        if (statement instanceof Select) {
            Select select = (Select) statement;
            return select(select.getSelectBody());
        }
        throw new SQLException("unrecognized command for query: " + command);
    }

    private ResultSet select(SelectBody selectBody) throws SQLException {
        MySelectVisitor visitor = new MySelectVisitor();
        selectBody.accept(visitor);
        final TableData tableData = lookUpTable(visitor.tableName);

        List selectItems = visitor.plainSelect.getSelectItems();
        if (selectItems.size() != 1) {
            throw new UnimplementedException("only one select item allowed (SELECT X not SELECT X, Y)");
        }
        SelectItem item = (SelectItem) selectItems.get(0);
        MySelectItemVisitor selectItemVisitor = new MySelectItemVisitor();
        item.accept(selectItemVisitor);
        Expression expression = selectItemVisitor.expression;
        
        MyExpressionVisitor expressionVisitor = new MyExpressionVisitor();
        expression.accept(expressionVisitor);
        final Column column = expressionVisitor.column;
        
        final int rowCount = tableData.rowCount();

        return new ResultSetStub() {
            int pos = -1;
            
            public boolean next() throws SQLException {
                if (pos + 1 >= rowCount) {
                    return false;
                } else {
                    ++pos;
                    return true;
                }
            }

            public int getInt(String columnName) throws SQLException {
                return tableData.getInt(column.getColumnName(), 0);
            }
            
        };
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
        List columnNames = columnNamesFromDefinitions(columns);
        tables.put(table.toLowerCase(), new TableData(columnNames));
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

    private void dropTable(String table) throws SQLException {
        if (tables.containsKey(table.toLowerCase())) {
            tables.remove(table.toLowerCase());
        } else {
            throw new SQLException("no such table " + table);
        }
    }

    private TableData lookUpTable(String table) throws SQLException {
        if (tables.containsKey(table.toLowerCase())) {
            return (TableData) tables.get(table.toLowerCase());
        } else {
            throw new SQLException("no such table " + table);
        }
    }


    private void insert(String table, List columns, ItemsList itemsList)
    throws SQLException {
        TableData tableData = lookUpTable(table);
        addRow(tableData, columns, itemsList);
    }

    // really, arguments should be the column names and such for *this table*
    public void addRow(TableData tableData, List columns, ItemsList itemsList) throws SQLException {
        List columnNames = new ArrayList();
        List values = new ArrayList();

        List items = walkList(itemsList);
        for (int i = 0; i < columns.size(); ++i) {
            Column column = (Column) columns.get(i);
            LongValue expression = (LongValue) items.get(i);
            columnNames.add(column.getColumnName());
            values.add(new Long(expression.getValue()));
        }
        
        tableData.addRow(columnNames, values);
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
     * {@link java.sql.DatabaseMetaData}, this method will go away or become
     * some kind of convenience method.
     */
    public Set tables() {
        return Collections.unmodifiableSet(tables.keySet());
    }

    /**
     * Column names in given table.
     * 
     * Once this functionality is implemented in
     * {@link java.sql.DatabaseMetaData}, this method will go away or become
     * some kind of convenience method.
     */
    public List columnNames(String tableName) throws SQLException {
        TableData tableData = lookUpTable(tableName);
        return tableData.columnNames();
    }

    /**
     * Number of rows in given table.
     * 
     * Just a temporary method until the 
     * {@link ResultSet} code is done - counting rows is generally not
     * what code should be doing (except perhaps some test code?).
     */
    public int rowCount(String tableName) throws SQLException {
        TableData tableData = lookUpTable(tableName);
        return tableData.rowCount();
    }

    /**
     * Get some data out. Probably just a temporary method until the
     * {@link ResultSet} code is done.
     */
    public int getInt(String tableName, String columnName, int rowIndex) throws SQLException {
        TableData tableData = lookUpTable(tableName);
        return tableData.getInt(columnName, rowIndex);
    }

}
