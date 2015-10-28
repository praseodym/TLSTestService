package tlstestservice.messages;

import tlstestservice.Utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class HandshakeMsg {
    protected byte type;
    protected int length;
    protected byte[] payload;

    public HandshakeMsg(byte type, int length, byte[] payload) {
        this.type = type;
        this.length = length;
        this.payload = payload;
    }

    public HandshakeMsg(InputStream msg) throws IOException {
        type = (byte) msg.read();
        length = Utils.getuint24((byte) msg.read(), (byte) msg.read(), (byte) msg.read());
        payload = new byte[length];
        msg.read(payload, 0, length);
    }

    public byte getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] getBytes() {
        byte[] out = new byte[payload.length + 4];

        out[0] = type;
        out[1] = (byte) (0xFF & (length >>> 16));
        out[2] = (byte) (0xFF & (length >>> 8));
        out[3] = (byte) (0xFF & length);

        for (int i = 0; i < payload.length; i++) {
            out[i + 4] = payload[i];
        }

        return out;
    }
}
