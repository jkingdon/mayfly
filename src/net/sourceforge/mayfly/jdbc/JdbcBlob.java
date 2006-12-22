package net.sourceforge.mayfly.jdbc;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.util.ImmutableByteArray;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

public class JdbcBlob implements Blob {

    private final ImmutableByteArray bytes;

    public JdbcBlob(ImmutableByteArray bytes) {
        this.bytes = bytes;
    }

    public InputStream getBinaryStream() throws SQLException {
        return bytes.asBinaryStream();
    }

    public byte[] getBytes(long pos, int length) throws SQLException {
        throw new UnimplementedException();
    }

    public long length() throws SQLException {
        return bytes.length();
    }

    public long position(byte[] pattern, long start) throws SQLException {
        throw new UnimplementedException();
    }

    public long position(Blob pattern, long start) throws SQLException {
        throw new UnimplementedException();
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new UnimplementedException();
    }

    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw new UnimplementedException();
    }

    public int setBytes(long pos, byte[] bytes, int offset, int len)
        throws SQLException {
        throw new UnimplementedException();
    }

    public void truncate(long len) throws SQLException {
        throw new UnimplementedException();
    }

}
