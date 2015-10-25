package tlstestservice.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import tlstestservice.TLS;
import tlstestservice.Utils;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class CertificateVerify extends HandshakeMsg {
	public CertificateVerify(byte[] algorithms, byte[] signature) throws IOException {
		super(TLS.HANDSHAKE_MSG_TYPE_CERTIFICATE_VERIFY, 0, new byte[] {});
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		outStream.write(algorithms);
		outStream.write(Utils.getbytes16(signature.length));
		outStream.write(signature);
		
		payload = outStream.toByteArray();
		length = payload.length;
	}
}
