package tlstestservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class TLSByteArrayInputStream extends ByteArrayInputStream {
    /*
     * Read 8, 16, 24, and 32 bit SSL integer data types, encoded
     * in standard big-endian form.
     */

    public TLSByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    public TLSByteArrayInputStream(byte[] buf, int offset, int len) {
        super(buf, offset, len);
    }

    public int getInt8() throws IOException {
        return read();
    }

    public int getInt16() throws IOException {
        return (getInt8() << 8) | getInt8();
    }

    public int getInt24() throws IOException {
        return (getInt8() << 16) | (getInt8() << 8) | getInt8();
    }

    public int getInt32() throws IOException {
        return (getInt8() << 24) | (getInt8() << 16)
                | (getInt8() << 8) | getInt8();
    }

    /*
     * Read byte vectors with 8, 16, and 24 bit length encodings.
     */

    public byte[] getBytes8() throws IOException {
        int len = getInt8();
        byte b[] = new byte[len];

        read(b, 0, len);
        return b;
    }

    public byte[] getBytes16() throws IOException {
        int len = getInt16();
        byte b[] = new byte[len];

        read(b, 0, len);
        return b;
    }

    public byte[] getBytes24() throws IOException {
        int len = getInt24();
        byte b[] = new byte[len];

        read(b, 0, len);
        return b;
    }
}
