package tlstestservice.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import tlstestservice.ProtocolVersion;
import tlstestservice.TLS;
import tlstestservice.Utils;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class ClientHello extends HandshakeMsg {
	ProtocolVersion protocolVersion;
	byte[] random;
	byte[] sessionId;
	byte[] cipherSuites;
	byte[] compressionMethods;
	byte[] extensions;
	
	public ClientHello(ProtocolVersion protocolVersion, byte[] random, byte[] sessionId, byte[] cipherSuites, byte[] compressionMethods, byte[] extensions) throws IOException {
		super(TLS.HANDSHAKE_MSG_TYPE_CLIENT_HELLO, 0, new byte[] {});
		
		ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
		payloadStream.write(protocolVersion.getBytes());
		payloadStream.write(random);
		payloadStream.write(Utils.getbytes8(sessionId.length));
		payloadStream.write(sessionId);
		payloadStream.write(Utils.getbytes16(cipherSuites.length));
		payloadStream.write(cipherSuites);
		payloadStream.write(Utils.getbytes8(compressionMethods.length));
		payloadStream.write(compressionMethods);
		if(extensions.length > 0) {
			payloadStream.write(Utils.getbytes16(extensions.length));
			payloadStream.write(extensions);
		}
		
		payload = payloadStream.toByteArray();
		length = payload.length;
	}
	
	public ClientHello(HandshakeMsg hs) {
		super(hs.getType(), hs.getLength(), hs.getPayload());
		
		// Parse payload
		payload = hs.getPayload();

		// ProtocolVersion
		protocolVersion = new ProtocolVersion(payload[0], payload[1]);
		// Random
		random = Arrays.copyOfRange(payload, 2, 34);
		
		// SessionID
		int lenSessionId = payload[34];
		sessionId = new byte[lenSessionId];
		sessionId = Arrays.copyOfRange(payload, 35, 35 + lenSessionId);
		
		// CipherSuite
		int lenCipherSuites = Utils.getuint16(payload[35 + lenSessionId], payload[36 + lenSessionId]);
		cipherSuites = Arrays.copyOfRange(payload, 37 + lenSessionId, 37 + lenSessionId + lenCipherSuites);
		
		// CompressionMethod
		int lenCompressionMethods = payload[37 + lenSessionId + lenCipherSuites];
		compressionMethods = Arrays.copyOfRange(payload, 38 + lenSessionId + lenCipherSuites, 38 + lenSessionId + lenCipherSuites + lenCompressionMethods);
		
		// Extensions
		if(payload.length > 38 + lenSessionId + lenCipherSuites + lenCompressionMethods) {
			int lenExtensions = Utils.getuint16(payload[38 + lenSessionId + lenCipherSuites + lenCompressionMethods], payload[39 + lenSessionId + lenCipherSuites + lenCompressionMethods]);
			extensions = Arrays.copyOfRange(payload, 40 + lenSessionId + lenCipherSuites + lenCompressionMethods, 40 + lenSessionId + lenCipherSuites + lenCompressionMethods + lenExtensions);
		}
	}
	
	public byte[] getRandom() {
		return random;
	}
	
	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}
}
