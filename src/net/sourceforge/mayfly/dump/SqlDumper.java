package net.sourceforge.mayfly.dump;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TableData;

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
        for (Iterator iter = store.anonymousSchema().tables().iterator(); 
            iter.hasNext();) {
            String tableName = (String) iter.next();
            createTable(tableName, store.table(tableName), out);
        }

        for (Iterator iter = store.anonymousSchema().tables().iterator(); 
            iter.hasNext();) {
            String tableName = (String) iter.next();
            rows(tableName, store.table(tableName), out);
        }
    }

    private void createTable(String tableName, TableData table, Writer out) 
    throws IOException {
        out.write("CREATE TABLE ");
        out.write(tableName);
        out.write("(\n");
        columns(table, out);
        out.write(");\n\n");
    }

    private void columns(TableData data, Writer out) throws IOException {
        for (Iterator iter = data.columns().iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            out.write("  ");
            out.write(column.columnName());
            out.write(" ");
            out.write(column.type.dumpName());
            if (iter.hasNext()) {
                out.write(",");
            }
            out.write("\n");
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
            out.write(row.cell(i).asBriefString());
            if (i < row.columnCount() - 1) {
                out.write(", ");
            }
        }
        out.write(");\n");
    }

}
