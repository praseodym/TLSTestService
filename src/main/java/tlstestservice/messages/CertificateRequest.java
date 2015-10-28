package tlstestservice.messages;

import tlstestservice.TLS;
import tlstestservice.TLSByteArrayInputStream;
import tlstestservice.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class CertificateRequest extends HandshakeMsg {
    byte[] cert_types;
    byte[] supported_algorithms;
    byte[] distinguished_names;

    public CertificateRequest(HandshakeMsg msg) throws IOException {
        super(msg.getType(), msg.getLength(), msg.getPayload());

        TLSByteArrayInputStream inStream = new TLSByteArrayInputStream(payload);
        cert_types = inStream.getBytes8();
        supported_algorithms = inStream.getBytes16();

        if (inStream.available() > 0) distinguished_names = inStream.getBytes16();

        inStream.close();
    }

    public CertificateRequest(byte[] cert_types, byte[] supported_algorithms, byte[] distinguished_names) throws IOException {
        super(TLS.HANDSHAKE_MSG_TYPE_CERTIFICATE_REQUEST, 0, new byte[]{});

        this.cert_types = cert_types;
        this.supported_algorithms = supported_algorithms;
        this.distinguished_names = distinguished_names;

        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        payloadStream.write(Utils.getbytes8(cert_types.length));
        payloadStream.write(cert_types);
        payloadStream.write(Utils.getbytes16(supported_algorithms.length));
        payloadStream.write(supported_algorithms);
        payloadStream.write(Utils.getbytes16(distinguished_names.length));
        payloadStream.write(distinguished_names);

        payload = payloadStream.toByteArray();
        length = payload.length;
    }

    public byte[] getSupportedAlgorithms() {
        return supported_algorithms;
    }
}
