package tlstestservice.messages;

import tlstestservice.TLS;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class Finished extends HandshakeMsg {
	public Finished(byte[] verifyData) {
		super(TLS.HANDSHAKE_MSG_TYPE_FINISHED, verifyData.length, verifyData);
	}
}