package tlstestservice;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class TLS12 extends TLS {
	public static final byte TLS_PRF_SHA256 = 0x01;
	
	public static byte PRFAlgorithm = TLS_PRF_SHA256; 
	
	public TLS12() {
		protocolVersion = ProtocolVersion.TLS12;
	}
	
	public static byte[] P_SHA256(byte[] secret, byte[] seed) throws Exception {
		byte[] output = {};
		byte[] A = seed;
		
		for(int i = 0; i < 4; i++) {
			A = Crypto.HMAC_SHA256(secret, A);
			output = Utils.concat(output, Crypto.HMAC_SHA256(secret, Utils.concat(A, seed)));
		}
		
		return output;
	}
	
	public static byte[] PRF(byte[] secret, String label, byte[] seed) throws InvalidKeyException, NoSuchAlgorithmException, Exception {
		if(PRFAlgorithm == TLS_PRF_SHA256)
			return P_SHA256(secret, Utils.concat(label.getBytes(), seed));
		else
			throw new Exception("Unknown PRFAlgorithm: " + PRFAlgorithm);
	}
	
	public byte[] masterSecret(byte[] preMasterSecret, byte[] serverRandom, byte[] clientRandom) throws Exception {
		return Arrays.copyOf(PRF(preMasterSecret, "master secret", Utils.concat(clientRandom, serverRandom)), 48);
	}
	
	public byte[] keyblock(byte[] masterSecret, byte[] serverRandom, byte[] clientRandom) throws Exception {
		return PRF(masterSecret, "key expansion", Utils.concat(serverRandom, clientRandom));
	}
	
	public byte[] verifyDataClient(byte[] masterSecret, byte[] handshakeMessages) throws Exception {
		if(PRFAlgorithm == TLS_PRF_SHA256)
			return Arrays.copyOf(PRF(masterSecret, "client finished", Crypto.SHA256(handshakeMessages)), 12);
		else
			throw new Exception("Unknown PRFAlgorithm: " + PRFAlgorithm);
	}
	
	public byte[] verifyDataServer(byte[] masterSecret, byte[] handshakeMessages) throws Exception {
		if(PRFAlgorithm == TLS_PRF_SHA256)
			return Arrays.copyOf(PRF(masterSecret, "server finished", Crypto.SHA256(handshakeMessages)), 12);
		else
			throw new Exception("Unknown PRFAlgorithm: " + PRFAlgorithm);
	}
}
