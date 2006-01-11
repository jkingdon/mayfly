package net.sourceforge.mayfly.dump;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

import java.io.*;
import java.util.*;

public class Dumper {

    /**
     * <p>Dump out a simple XML version of the data in a data store.</p>
     * 
     * <p>For example, if you are debugging a test and want to see what is
     * in a {@link net.sourceforge.mayfly.Database} at a given moment:</p>
     * <tt>new Dumper().dump(database.dataStore(), System.out)</tt>
     */
    public void dump(DataStore store, Writer out) throws IOException {
        for (Iterator iter = store.anonymousSchema().tables().iterator(); iter.hasNext();) {
            String tableName = (String) iter.next();
            dump(store, tableName, out);
        }
    }

    /**
     * Dump out a simple XML version of the data in a single table of a data store.
     * 
     * <p>For example, if you are debugging a test and want to see what is
     * in one table of a {@link net.sourceforge.mayfly.Database} at a given moment:</p>
     * <tt>new Dumper().dump(database.dataStore(), "INVOICES", System.out)</tt>
     */
    public void dump(DataStore store, String tableName, Writer out) throws IOException {
        TableData table = store.table(DataStore.ANONYMOUS_SCHEMA_NAME, tableName);
        for (Iterator iter = table.rows().iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            dumpRow(tableName, row, out);
        }
    }

    private void dumpRow(String tableName, Row row, Writer out) throws IOException {
        XmlWriter xml = new XmlWriter();
        xml.startTag(tableName);
        xml.newline();
        Columns columns = row.columns();
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();

            xml.indent(2);
            xml.startTag(column.columnName());
            xml.text(row.cell(column).asObject().toString());
            xml.endTag(column.columnName());
            xml.newline();
        }
        xml.endTag(tableName);
        xml.newline();
        xml.newline();
        out.write(xml.getOutput());
    }

}
