package tlstestservice.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import tlstestservice.TLS;
import tlstestservice.TLSByteArrayInputStream;
import tlstestservice.Utils;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class Certificate extends HandshakeMsg {
	PublicKey pubKey;
	
	public Certificate(HandshakeMsg msg) throws IOException, CertificateException {
		super(msg.getType(),msg.getLength(), msg.getPayload());

		TLSByteArrayInputStream inStream = new TLSByteArrayInputStream(payload);
		
		// Read chain length
		int chain_len = inStream.getInt24();
		if(chain_len <= 0) {
			pubKey = null;
			inStream.close();
			return;
		}
		
		// Read certificate length
		int cert_len = inStream.getInt24();
		if(cert_len <= 0) {
			pubKey = null;
			inStream.close();
			return;
		}
		
		// Only read first certificate		
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);

		// Extract public key
		pubKey = cert.getPublicKey();
	}
	
	public Certificate(X509Certificate[] certs) throws CertificateEncodingException, IOException {
		super(TLS.HANDSHAKE_MSG_TYPE_CERTIFICATE, 0, new byte[] {});
		
		ByteArrayOutputStream chainStream = new ByteArrayOutputStream();
		
		for(int i = 0; i < certs.length; i++) {
			// Add 3 byte size
			chainStream.write(Utils.getbytes24(certs[i].getEncoded().length));
			// Add certificate
			chainStream.write(certs[i].getEncoded());
		}
		
		byte[] chain = chainStream.toByteArray();
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		outStream.write(Utils.getbytes24(chain.length));
		outStream.write(chain);
		
		payload = outStream.toByteArray();
		length = payload.length;
	}
	
	public PublicKey getPublicKey() {
		return pubKey;
	}

}
