package net.sourceforge.mayfly.dump;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.parser.Lexer;
import net.sourceforge.mayfly.parser.TokenType;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SqlDumper {

    public String dump(DataStore store) {
        StringWriter out = new StringWriter();
        try {
            dump(store, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    public void dump(DataStore store, Writer out) throws IOException {
        List sortedTables = sortTables(store);

        definition(store, sortedTables, out);

        data(store, sortedTables, out);
    }

    public void data(DataStore store, Writer out) 
    throws IOException {
        List sortedTables = sortTables(store);
        data(store, sortedTables, out);
    }

    private void definition(DataStore store, List sortedTables, Writer out) 
    throws IOException {
        for (Iterator iter = sortedTables.iterator(); iter.hasNext();) {
            String tableName = (String) iter.next();
            createTable(tableName, store.table(tableName), out);
        }
    }

    private List sortTables(final DataStore store) {
        Set tables = store.anonymousSchema().tables();
        List list = new ArrayList(tables);
        /*
        final Evaluator evaluator = new StoreEvaluator(
            store, DataStore.ANONYMOUS_SCHEMA_NAME);
        */
        Collections.sort(list, new Comparator() {

            public int compare(Object left, Object right) {
                String leftTable = (String) left;
                String rightTable = (String) right;
                /* I believe this is not really right - the Comparator
                 * isn't transitive and the circular reference detection
                 * is, uh, limited.
                 */
                /*
                boolean leftRefersToRight = 
                    store.table(leftTable).constraints
                        .refersTo(rightTable, evaluator);
                boolean rightRefersToLeft = 
                    store.table(rightTable).constraints
                        .refersTo(leftTable, evaluator);
                if (leftRefersToRight && rightRefersToLeft) {
                    throw new MayflyException(
                        "cannot dump: circular reference between tables " + 
                        leftTable + " and " + rightTable);
                }
                else if (leftRefersToRight) {
                    return 1;
                }
                else if (rightRefersToLeft) {
                    return -1;
                }
                else {
                */
                    /* It might be desirable, in some contexts, to preserve
                       original order (for example, if you want to look over
                       the dump file and see related tables grouped together,
                       assuming they were in the original script which loaded
                       the data).
                       
                       However, for other things, like comparing two dumps,
                       having the order not depend on the order tables was
                       inserted is good.  This is what we implement for now.
                       
                       This may want to be an option some day.
                    */
                    return leftTable.compareTo(rightTable);
                /*
                }
                */
            } 
            
        });
        return list;
    }

    private void createTable(String tableName, TableData table, Writer out) 
    throws IOException {
        out.write("CREATE TABLE ");
        identifier(tableName, out);
        out.write("(\n");
        columns(table, out);
        constraints(table.constraints, out);
        out.write(");\n\n");
    }

    public static void identifier(String text, Writer out) throws IOException {
        if (TokenType.lookupKeyword(text) != null) {
            out.write("\"");
            out.write(text);
            out.write("\"");
        }
        else if (looksLikeIdentifier(text)) {
            out.write(text);
        }
        else {
            out.write("\"");
            out.write(text);
            out.write("\"");
        }
    }

    private static boolean looksLikeIdentifier(String text) {
        if (text.indexOf('\"') != -1) {
            throw new MayflyException(
                "don't know how to dump identifier containing a double quote"
            );
        }
        if (text.length() == 0) {
            throw new MayflyInternalException(
                "shouldn't have empty string as identifier");
        }
        if (!Lexer.isIdentifierStart(text.charAt(0))) {
            return false;
        }
        for (int i = 1; i < text.length(); ++i) {
            if (!Lexer.isIdentifierCharacter(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void columns(TableData data, Writer out) throws IOException {
        for (Iterator iter = data.columns().iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            column(column, out);
            if (iter.hasNext() || data.constraints.constraintCount() > 0) {
                out.write(",");
            }
            out.write("\n");
        }
    }

    private void column(Column column, Writer out) throws IOException {
        out.write("  ");
        identifier(column.columnName(), out);
        out.write(" ");
        out.write(column.type.dumpName());
        if (column.hasDefault()) {
            out.write(" DEFAULT ");
            out.write(column.defaultValueAsSql());
        }
        
        if (column.hasOnUpdateValue()) {
            out.write(" ON UPDATE ");
            out.write(column.onUpdateValueAsSql());
        }
        
        if (column.isAutoIncrement()) {
            out.write(" AUTO_INCREMENT");
        }

        if (column.isNotNull) {
            out.write(" NOT NULL");
        }
    }

    private void constraints(Constraints constraints, Writer out) 
    throws IOException {
        for (int i = 0; i < constraints.constraintCount(); ++i) {
            Constraint constraint = constraints.constraint(i);
            out.write("  ");
            if (constraint.constraintName != null) {
                out.write("CONSTRAINT ");
                out.write(constraint.constraintName);
                out.write(" ");
            }
            constraint.dump(out);
            if (i < constraints.constraintCount() - 1) {
                out.write(",");
            }
            out.write("\n");
        }
    }

    public void data(DataStore store, List sortedTables, Writer out) 
    throws IOException {
        for (Iterator iter = sortedTables.iterator(); iter.hasNext();) {
            String tableName = (String) iter.next();
            rows(tableName, store.table(tableName), out);
        }
    }

    private void rows(String tableName, TableData table, Writer out) 
    throws IOException {
        Collection rows = sortRows(table);
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            row(tableName, row, out);
        }
        
        if (rows.size() > 0) {
            out.write("\n");
        }
    }

    private Collection sortRows(final TableData table) {
        List result = new ArrayList();
        for (int i = 0; i < table.rowCount(); ++i) {
            Row row = table.row(i);
            result.add(row);
        }
        /* It seems like we need both a transitive closure and
           a topological sort (that is, we know row A should go
           before row B and row B before row C, so we need to
           deduce that A goes before C, and arrange everything in
           a single sorted list (throwing an exception if it
           can't be done - that is, if there are cycles). */
        Collections.sort(result, new Comparator() {

            public int compare(Object object1, Object object2) {
                Row first = (Row) object1;
                Row second = (Row) object2;
                /* I believe this is not really right - the Comparator
                 * isn't transitive and the circular reference detection
                 * isn't there yet.
                 */
//                return table.constraints.requiredInsertionOrder(first, second);
                return naturalOrder(first, second);
            }

            private int naturalOrder(Row first, Row second) {
                for (int i = 0; i < first.columnCount(); ++i) {
                    Cell cellFromFirst = first.cell(i);
                    Cell cellFromSecond = second.cell(i);
                    int comparison = cellFromFirst.compareTo(cellFromSecond);
                    if (comparison != 0) {
                        return comparison;
                    }
                }
                return 0;
            } 
            
        });
        return result;
    }
    
    private void row(String tableName, Row row, Writer out) throws IOException {
        out.write("INSERT INTO ");
        out.write(tableName);
        out.write("(");
        for (int i = 0; i < row.columnCount(); ++i) {
            out.write(row.columnName(i));
            if (i < row.columnCount() - 1) {
                out.write(", ");
            }
        }
        out.write(") VALUES(");
        for (int i = 0; i < row.columnCount(); ++i) {
            out.write(row.cell(i).asSql());
            if (i < row.columnCount() - 1) {
                out.write(", ");
            }
        }
        out.write(");\n");
    }

}
