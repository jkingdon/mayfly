package net.sourceforge.mayfly.datastore;

import java.io.InputStream;
import java.sql.SQLException;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.util.ImmutableByteArray;

public class BinaryCell extends Cell {

    private final ImmutableByteArray data;

    public BinaryCell(ImmutableByteArray array) {
        this.data = array;
    }

    public String asBriefString() {
        return "binary data";
    }

    public int compareTo(Cell otherCell) {
        throw new UnimplementedException();
    }

    public String displayName() {
        return "binary data";
    }
    
    public InputStream asBinaryStream() throws SQLException {
        return data.asBinaryStream();
    }

}
