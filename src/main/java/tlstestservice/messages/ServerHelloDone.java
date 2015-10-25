package tlstestservice.messages;

import tlstestservice.TLS;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class ServerHelloDone extends HandshakeMsg {
	public ServerHelloDone() {
		super(TLS.HANDSHAKE_MSG_TYPE_SERVER_HELLO_DONE, 0, new byte[] {});
	}
}
