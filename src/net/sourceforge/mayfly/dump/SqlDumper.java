package net.sourceforge.mayfly.dump;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
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
import java.util.Iterator;

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
        definition(store, out);

        data(store, out);
    }

    private void definition(DataStore store, Writer out) throws IOException {
        for (Iterator iter = store.anonymousSchema().tables().iterator(); 
            iter.hasNext();) {
            String tableName = (String) iter.next();
            createTable(tableName, store.table(tableName), out);
        }
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
            out.write(column.defaultValue().asSql());
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

    public void data(DataStore store, Writer out) throws IOException {
        for (Iterator iter = store.anonymousSchema().tables().iterator(); 
            iter.hasNext();) {
            String tableName = (String) iter.next();
            rows(tableName, store.table(tableName), out);
        }
    }

    private void rows(String tableName, TableData table, Writer out) 
    throws IOException {
        for (int i = 0; i < table.rowCount(); ++i) {
            Row row = table.row(i);
            row(tableName, row, out);
        }
        
        if (table.rowCount() > 0) {
            out.write("\n");
        }
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
