package net.sourceforge.mayfly.datastore;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import net.sourceforge.mayfly.MayflySqlException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.jdbc.JdbcBlob;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableByteArray;

public class BinaryCell extends Cell {

    private final ImmutableByteArray data;

    public BinaryCell(ImmutableByteArray array) {
        this.data = array;
    }

    public BinaryCell(byte[] array) {
        this.data = new ImmutableByteArray(array);
    }

    public BinaryCell(byte singleByte) {
        this(new byte[] { singleByte });
    }

    public String asBriefString() {
        return "binary data";
    }

    public int compareTo(Cell otherCell, Location location) {
        throw new UnimplementedException(location);
    }

    public String displayName() {
        return "binary data";
    }
    
    public String asSql() {
        return data.asSql();
    }
    
    public InputStream asBinaryStream() throws SQLException {
        return data.asBinaryStream();
    }
    
    public Blob asBlob() throws MayflySqlException {
        return new JdbcBlob(data);
    }
    
    public byte[] asBytes() throws SQLException {
        return data.asBytes();
    }

}
